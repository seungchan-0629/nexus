package com.example.statistics.domain.daily;
import com.example.statistics.domain.daily.dto.DumpResult;
import com.example.statistics.domain.daily.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyDumpInternalService {

    private final StringRedisTemplate redisTemplate;
    private final DailyTotalSalesRepository dailyTotalSalesRepository;
    private final DailyStoreSalesRepository dailyStoreSalesRepository;
    private final DailyMenuSalesRepository dailyMenuSalesRepository;
    private final DailyHourlySalesRepository dailyHourlySalesRepository;
    private final DailyCategorySalesRepository dailyCategorySalesRepository;
    private final DailyPaymentSalesRepository dailyPaymentSalesRepository;
    private final DailyDumpLogRepository dailyDumpLogRepository;

    /** 메인 dump 로직 (트랜잭션 적용, 외부 클래스에서 호출되어야 동작) */
    @Transactional
    public DumpResult doDump(LocalDate date, long start) {
        int totalCount    = dumpTotal(date);
        int storeCount    = dumpStore(date);
        int menuCount     = dumpMenu(date);
        int hourlyCount   = dumpHourly(date);
        int categoryCount = dumpCategory(date);
        int paymentCount  = dumpPayment(date);

        int recordTotal = totalCount + storeCount + menuCount
                + hourlyCount + categoryCount + paymentCount;

        dailyDumpLogRepository.save(DailyDumpLog.builder()
                .aggregateDate(date)
                .dumpStatus(DumpStatus.SUCCESS)
                .recordCount(recordTotal)
                .build());

        long elapsed = System.currentTimeMillis() - start;
        return DumpResult.success(date, totalCount, storeCount, menuCount,
                hourlyCount, categoryCount, paymentCount, elapsed);
    }

    /** 실패 로그 기록 (별도 트랜잭션 — 메인 롤백돼도 살아남음) */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(LocalDate date, String errorMessage) {
        dailyDumpLogRepository.save(DailyDumpLog.builder()
                .aggregateDate(date)
                .dumpStatus(DumpStatus.FAILED)
                .recordCount(0)
                .errorMessage(errorMessage)
                .build());
    }

    private int dumpTotal(LocalDate date) {
        String value = redisTemplate.opsForValue().get("sales:today:" + date);

        if (value == null) {
            return 0;
        }

        dailyTotalSalesRepository.save(DailyTotalSales.builder().aggregateDate(date).totalAmount(Long.parseLong(value)).build());

        return 1;
    }

    private int dumpStore(LocalDate date) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores("sales:store:ranking:" + date, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            return 0;
        }

        List<DailyStoreSales> entities = tuples.stream()
                .map(t -> {
                    String[] parts = t.getValue().split("\\|", 2);  // "idx|name"
                    return DailyStoreSales.builder()
                            .aggregateDate(date)
                            .storeIdx(Long.parseLong(parts[0]))
                            .storeName(parts[1])
                            .amount(t.getScore() == null ? 0L : t.getScore().longValue())
                            .build();
                })
                .toList();

        dailyStoreSalesRepository.saveAll(entities);

        return entities.size();
    }

    private int dumpMenu(LocalDate date) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores("sales:menu:ranking:" + date, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            return 0;
        }

        List<DailyMenuSales> entities = tuples.stream()
                .map(t -> {
                    String[] parts = t.getValue().split("\\|", 2);
                    return DailyMenuSales.builder()
                            .aggregateDate(date)
                            .menuIdx(Long.parseLong(parts[0]))
                            .menuName(parts[1])
                            .quantity(t.getScore() == null ? 0L : t.getScore().longValue())
                            .build();
                })
                .toList();

        dailyMenuSalesRepository.saveAll(entities);

        return entities.size();
    }

    private int dumpHourly(LocalDate date) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("sales:hourly:" + date);

        if (entries.isEmpty()) {
            return 0;
        }

        List<DailyHourlySales> entities = entries.entrySet().stream()
                .map(e -> DailyHourlySales.builder()
                        .aggregateDate(date)
                        .hour(Integer.parseInt((String) e.getKey()))
                        .amount(Long.parseLong((String) e.getValue()))
                        .build())
                .toList();

        dailyHourlySalesRepository.saveAll(entities);

        return entities.size();
    }

    private int dumpCategory(LocalDate date) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("sales:category:" + date);

        if (entries.isEmpty()) {
            return 0;
        }

        List<DailyCategorySales> entities = entries.entrySet().stream()
                .map(e -> DailyCategorySales.builder()
                        .aggregateDate(date)
                        .categoryName((String) e.getKey())
                        .amount(Long.parseLong((String) e.getValue()))
                        .build())
                .toList();

        dailyCategorySalesRepository.saveAll(entities);

        return entities.size();
    }

    private int dumpPayment(LocalDate date) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("sales:payment:" + date);

        if (entries.isEmpty()) {
            return 0;
        }

        List<DailyPaymentSales> entities = entries.entrySet().stream()
                .map(e -> DailyPaymentSales.builder()
                        .aggregateDate(date)
                        .paymentMethod((String) e.getKey())
                        .amount(Long.parseLong((String) e.getValue()))
                        .build())
                .toList();

        dailyPaymentSalesRepository.saveAll(entities);

        return entities.size();
    }

}
