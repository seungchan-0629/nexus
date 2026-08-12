package com.example.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 재고 부족으로 처리 실패한 주문을 일괄 롤백하고 REJECT 처리한다.
 * <p>
 * 개선 전: orderId 루프 × (3 + 4N) 쿼리 → 주문 2,000건 × 아이템 5개 = 약 46,000 쿼리
 * 개선 후: 거절 주문 전체를 5개의 bulk SQL로 일괄 처리 (주문 수 무관)
 * <p>
 * 실행 순서 (processed=true 마커를 기준으로 복구 후 리셋):
 *   ① HeadInventory 재고 복구  – product별 delta 합산 JOIN UPDATE
 *   ② StoreInventory 삭제      – orders_item_idx 기준 JOIN DELETE
 *   ③ InventoryMovement 삭제   – orders_idx IN 쿼리
 *   ④ OrdersItem processed 리셋 – IN 쿼리 UPDATE
 *   ⑤ Orders REJECT            – IN 쿼리 UPDATE
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RejectInsufficientOrdersTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 스테이징 기준으로 processed=false 가 남아있는 주문을 거절 대상으로 조회.
     * 배치 실행 중 새로 유입된 발주(스테이징에 없음)는 대상에서 자동 제외된다.
     */
    private static final String INSUFFICIENT_ORDER_IDS_SQL = """
            SELECT DISTINCT s.orders_idx
            FROM order_batch.orders_item_staging s
            JOIN orders_item oi ON s.orders_item_idx = oi.orders_item_idx
            WHERE oi.processed = false
            ORDER BY s.orders_idx ASC
            """;

    /** ① HeadInventory 재고 복구
     *  - product별 처리 완료 수량(delta)을 합산해 한 번에 UPDATE
     *  - SET 좌→우 평가 순서: CASE 평가 시점의 h.count 는 이미 (old + delta) 값
     *  - Error 1020 없음: Step1.5 는 단일 스레드이므로 JOIN UPDATE 안전
     */
    private static final String RESTORE_HEAD_INVENTORY_SQL = """
            UPDATE head_inventory h
            JOIN (
                SELECT oi.product_idx,
                       SUM(oi.count) AS delta,
                       p.min_stock
                FROM   orders_item oi
                JOIN   product p ON oi.product_idx = p.product_idx
                WHERE  oi.orders_idx IN (:rejectedIds)
                  AND  oi.processed  = true
                GROUP  BY oi.product_idx, p.min_stock
            ) agg ON h.product_idx = agg.product_idx
            SET h.count  = h.count + agg.delta,
                h.status = CASE
                    WHEN h.count <= 0                  THEN 'CRITICAL'
                    WHEN h.count <= agg.min_stock / 2  THEN 'CRITICAL'
                    WHEN h.count <= agg.min_stock      THEN 'LOW'
                    ELSE 'NORMAL'
                END
            """;

    /** ② StoreInventory 삭제 – processed=true 항목만 대상 */
    private static final String DELETE_STORE_INVENTORY_SQL = """
            DELETE si FROM store_inventory si
            JOIN orders_item oi ON si.orders_item_idx = oi.orders_item_idx
            WHERE oi.orders_idx IN (:rejectedIds)
              AND oi.processed  = true
            """;

    /** ③ InventoryMovement 삭제 */
    private static final String DELETE_INVENTORY_MOVEMENT_SQL = """
            DELETE FROM inventory_movement
            WHERE orders_idx IN (:rejectedIds)
            """;

    /** ④ OrdersItem processed 리셋 (① 이후에 실행) */
    private static final String RESET_ORDERS_ITEM_SQL = """
            UPDATE orders_item
            SET    processed = false
            WHERE  orders_idx IN (:rejectedIds)
              AND  processed  = true
            """;

    /** ⑤ Orders REJECT */
    private static final String REJECT_ORDERS_SQL = """
            UPDATE orders
            SET    order_status = 'REJECT'
            WHERE  orders_idx IN (:rejectedIds)
            """;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Long> orderIds = jdbcTemplate.queryForList(INSUFFICIENT_ORDER_IDS_SQL, Long.class);
        log.info("[Step1.5] 재고 부족 REJECT 대상: {}건", orderIds.size());

        if (orderIds.isEmpty()) {
            log.info("[Step1.5] REJECT 대상 없음, 스킵");
            return RepeatStatus.FINISHED;
        }

        MapSqlParameterSource params = new MapSqlParameterSource("rejectedIds", orderIds);

        int headUpdated  = namedParameterJdbcTemplate.update(RESTORE_HEAD_INVENTORY_SQL, params);
        log.info("[Step1.5] ① HeadInventory 복구: {}개 product", headUpdated);

        int storeDeleted = namedParameterJdbcTemplate.update(DELETE_STORE_INVENTORY_SQL, params);
        log.info("[Step1.5] ② StoreInventory 삭제: {}건", storeDeleted);

        int movDeleted   = namedParameterJdbcTemplate.update(DELETE_INVENTORY_MOVEMENT_SQL, params);
        log.info("[Step1.5] ③ InventoryMovement 삭제: {}건", movDeleted);

        int itemReset    = namedParameterJdbcTemplate.update(RESET_ORDERS_ITEM_SQL, params);
        log.info("[Step1.5] ④ OrdersItem 리셋: {}건", itemReset);

        int rejected     = namedParameterJdbcTemplate.update(REJECT_ORDERS_SQL, params);
        log.info("[Step1.5] ⑤ Orders REJECT: {}건", rejected);

        contribution.incrementWriteCount(rejected);
        return RepeatStatus.FINISHED;
    }
}
