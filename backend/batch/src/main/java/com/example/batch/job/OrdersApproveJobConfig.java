package com.example.batch.job;

import com.example.batch.domain.orders.model.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class OrdersApproveJobConfig {

    private static final int STEP1_CHUNK_SIZE = 50;
    private static final int STEP2_CHUNK_SIZE = 100;
    private static final int PARTITION_THREAD_COUNT = 5;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final ProductPartitioner productPartitioner;
    private final ProductInventoryWriter productInventoryWriter;
    private final OrderApproveProcessor orderApproveProcessor;
    private final OrderApproveStepWriter orderApproveStepWriter;
    private final RejectInsufficientOrdersTasklet rejectInsufficientOrdersTasklet;
    private final PrepareOrdersStagingTasklet prepareOrdersStagingTasklet;

    @Bean
    public Job approveConfirmedOrdersJob() {
        return new JobBuilder("approveConfirmedOrdersJob", jobRepository)
                .start(prepareOrdersStagingStep())          // Step0: 스냅샷 고정
                .next(productProcessPartitionStep())         // Step1: 병렬 재고 처리
                .next(rejectInsufficientOrdersStep())        // Step1.5: 재고 부족 롤백
                .next(orderApproveStep())                    // Step2: 최종 승인
                .build();
    }

    // ── Step0: 스테이징 테이블 스냅샷 ────────────────────────────────────

    @Bean
    public Step prepareOrdersStagingStep() {
        return new StepBuilder("prepareOrdersStagingStep", jobRepository)
                .tasklet(prepareOrdersStagingTasklet, transactionManager)
                .build();
    }

    // ── Step1: product_id 기준 파티션 병렬 처리 ──────────────────────────

    @Bean
    public Step productProcessPartitionStep() {
        return new StepBuilder("productProcessPartitionStep", jobRepository)
                .partitioner("productProcessWorkerStep", productPartitioner)
                .step(productProcessWorkerStep())
                .partitionHandler(productPartitionHandler())
                .build();
    }

    @Bean
    public PartitionHandler productPartitionHandler() {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(productProcessWorkerStep());
        handler.setTaskExecutor(productTaskExecutor());
        handler.setGridSize(PARTITION_THREAD_COUNT);
        return handler;
    }

    @Bean
    public ThreadPoolTaskExecutor productTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(PARTITION_THREAD_COUNT);
        executor.setMaxPoolSize(PARTITION_THREAD_COUNT);
        executor.setThreadNamePrefix("product-partition-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step productProcessWorkerStep() {
        return new StepBuilder("productProcessWorkerStep", jobRepository)
                .<OrdersItemRow, OrdersItemRow>chunk(STEP1_CHUNK_SIZE, transactionManager)
                .reader(ordersItemByProductReader(null))
                .writer(productInventoryWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<OrdersItemRow> ordersItemByProductReader(
            @Value("#{stepExecutionContext['productId']}") Long productId) {
        return new JdbcCursorItemReaderBuilder<OrdersItemRow>()
                .name("ordersItemByProductReader")
                .dataSource(dataSource)
                .sql("""
                        SELECT orders_item_idx, count, orders_idx,
                               store_idx, product_idx, min_stock
                        FROM order_batch.orders_item_staging
                        WHERE product_idx = ?
                        ORDER BY orders_idx ASC
                        """)
                .preparedStatementSetter(ps -> ps.setLong(1, productId))
                .rowMapper((rs, rowNum) -> new OrdersItemRow(
                        rs.getLong("orders_item_idx"),
                        rs.getInt("count"),
                        rs.getLong("orders_idx"),
                        rs.getLong("store_idx"),
                        rs.getLong("product_idx"),
                        rs.getInt("min_stock")
                ))
                .build();
    }

    // ── Step1.5: 재고 부족 주문 롤백 + REJECT ────────────────────────────

    @Bean
    public Step rejectInsufficientOrdersStep() {
        return new StepBuilder("rejectInsufficientOrdersStep", jobRepository)
                .tasklet(rejectInsufficientOrdersTasklet, transactionManager)
                .build();
    }

    // ── Step2: 전체 처리 완료된 Order만 승인 ─────────────────────────────

    @Bean
    public Step orderApproveStep() {
        return new StepBuilder("orderApproveStep", jobRepository)
                .<Long, Orders>chunk(STEP2_CHUNK_SIZE, transactionManager)
                .reader(fullyProcessedOrderReader())
                .processor((ItemProcessor<Long, Orders>) orderApproveProcessor::process)
                .writer(orderApproveStepWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Long> fullyProcessedOrderReader() {
        return new JdbcCursorItemReaderBuilder<Long>()
                .name("fullyProcessedOrderReader")
                .dataSource(dataSource)
                .sql("""
                        SELECT DISTINCT s.orders_idx
                        FROM order_batch.orders_item_staging s
                        JOIN orders o ON s.orders_idx = o.orders_idx
                        WHERE o.order_status = 'CONFIRMED'
                          AND NOT EXISTS (
                              SELECT 1
                              FROM order_batch.orders_item_staging s2
                              JOIN orders_item oi ON s2.orders_item_idx = oi.orders_item_idx
                              WHERE s2.orders_idx = s.orders_idx
                                AND oi.processed  = false
                          )
                        ORDER BY s.orders_idx ASC
                        """)
                .rowMapper((rs, rowNum) -> rs.getLong("orders_idx"))
                .build();
    }
}
