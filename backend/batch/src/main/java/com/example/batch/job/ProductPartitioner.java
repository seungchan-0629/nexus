package com.example.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductPartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // PrepareOrdersStagingTasklet 이 고정한 스냅샷 기준으로 파티션 생성
        // → 배치 실행 중 새로 유입된 CONFIRMED 발주가 파티션에 끼어들지 않음
        List<Long> productIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT product_idx FROM order_batch.orders_item_staging",
                Long.class
        );

        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
        for (Long productId : productIds) {
            ExecutionContext ctx = new ExecutionContext();
            ctx.putLong("productId", productId);
            partitions.put("partition_product_" + productId, ctx);
        }
        return partitions;
    }
}
