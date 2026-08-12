package com.example.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 배치 실행 첫 번째 Step.
 * <p>
 * 이 시점의 CONFIRMED 발주를 orders_item_staging 에 스냅샷으로 저장한다.
 * 이후 Step들은 라이브 테이블 대신 스테이징 테이블을 기준으로 동작하므로,
 * 배치 실행 중 새로 유입된 CONFIRMED 발주가 끼어들거나
 * Step1.5에서 오판(미처리 = 재고부족)되는 문제를 방지한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrepareOrdersStagingTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_DB_SQL =
            "CREATE DATABASE IF NOT EXISTS order_batch";

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS order_batch.orders_item_staging (
                orders_item_idx BIGINT NOT NULL,
                count           INT    NOT NULL,
                orders_idx      BIGINT NOT NULL,
                store_idx       BIGINT NOT NULL,
                product_idx     BIGINT NOT NULL,
                min_stock       INT    NOT NULL,
                PRIMARY KEY (orders_item_idx)
            )
            """;

    private static final String TRUNCATE_SQL =
            "TRUNCATE TABLE order_batch.orders_item_staging";

    private static final String INSERT_SQL = """
            INSERT INTO order_batch.orders_item_staging
                (orders_item_idx, count, orders_idx, store_idx, product_idx, min_stock)
            SELECT oi.orders_item_idx, oi.count, oi.orders_idx,
                   o.store_idx, oi.product_idx, p.min_stock
            FROM orders_item oi
            JOIN orders  o ON oi.orders_idx  = o.orders_idx
            JOIN product p ON oi.product_idx = p.product_idx
            WHERE o.order_status = 'CONFIRMED'
              AND oi.processed   = false
            ORDER BY o.orders_idx ASC
            """;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        jdbcTemplate.execute(CREATE_DB_SQL);
        jdbcTemplate.execute(CREATE_TABLE_SQL);
        jdbcTemplate.execute(TRUNCATE_SQL);

        int inserted = jdbcTemplate.update(INSERT_SQL);
        log.info("[스테이징] orders_item_staging 스냅샷 완료: {}건", inserted);
        contribution.incrementWriteCount(inserted);

        return RepeatStatus.FINISHED;
    }
}
