package com.example.batch.job;

import com.example.batch.common.enums.DeliveryStatus;
import com.example.batch.domain.delivery.DeliveryRepository;
import com.example.batch.domain.delivery.model.Delivery;
import com.example.batch.domain.orders.model.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApproveStepWriter implements ItemWriter<Orders> {

    private final DeliveryRepository deliveryRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT_HEAD_INCOME_SQL = """
            INSERT INTO head_income (price, status, settlement_idx, store_idx, orders_idx)
            VALUES (:price, :status, :settlementIdx, :storeIdx, :ordersIdx)
            """;

    /**
     * 개선 전: 청크 100건 처리 시 DB 왕복 200회
     *   - findByOrdersForUpdate()      × 100 = 100회 SELECT
     *   - headIncomeRepository.save()  × 100 = 100회 INSERT
     *
     * 개선 후: 청크 100건 처리 시 DB 왕복 2회
     *   - findAllByOrdersIdxIn()       × 1   = 1회  SELECT (IN 쿼리)
     *   - batchUpdate()                × 1   = 1회  JDBC batch INSERT
     */
    @Override
    public void write(Chunk<? extends Orders> chunk) {
        List<Orders> ordersList = new ArrayList<>(chunk.getItems());
        List<Long> orderIds = ordersList.stream()
                .map(Orders::getIdx)
                .toList();

        // ① 모든 주문 APPROVED 처리 (JPA 더티체킹 → 커밋 시 일괄 UPDATE)
        ordersList.forEach(Orders::approve);

        // ② Delivery 청크 단위 일괄 조회 + STATUS 업데이트
        //    findByOrdersForUpdate() N회 → findAllByOrdersIdxIn() 1회
        Map<Long, Delivery> deliveryMap = deliveryRepository
                .findAllByOrdersIdxIn(orderIds)
                .stream()
                .collect(Collectors.toMap(d -> d.getOrders().getIdx(), d -> d));

        ordersList.forEach(orders -> {
            Delivery delivery = deliveryMap.get(orders.getIdx());
            if (delivery != null && delivery.getDeliveryStatus() == DeliveryStatus.READY) {
                delivery.setDeliveryStatus(DeliveryStatus.START);
            }
        });

        // ③ HeadIncome JDBC batch INSERT
        //    IDENTITY 전략은 Hibernate가 배치 INSERT를 지원하지 않으므로
        //    NamedParameterJdbcTemplate.batchUpdate() 로 직접 처리
        SqlParameterSource[] params = ordersList.stream()
                .map(orders -> new MapSqlParameterSource()
                        .addValue("price",        orders.getPrice())
                        .addValue("status",       false)
                        .addValue("settlementIdx", null)
                        .addValue("storeIdx",     orders.getStore().getIdx())
                        .addValue("ordersIdx",    orders.getIdx()))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(INSERT_HEAD_INCOME_SQL, params);

        log.info("[Step2] {}건 승인 완료 (orderId {} ~ {})",
                ordersList.size(), orderIds.get(0), orderIds.get(orderIds.size() - 1));
    }
}
