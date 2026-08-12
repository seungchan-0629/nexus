package com.example.statistics;

import com.example.statistics.model.StatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StringRedisTemplate redisTemplate;

    /**
    * 1. 오늘 총 매출
    * - GET sales:today:DATE
    * */
    public StatisticsDto.TodaySalesRes getTodaySales() {
        String key = "sales:today:" + LocalDate.now();
        String value = redisTemplate.opsForValue().get(key);
        long total = (value == null) ? 0L : Long.parseLong(value);
        return StatisticsDto.TodaySalesRes.builder().todaySales(total).build();
    }
    /**
     * 2. 매장 매출 랭킹 TOP 5
     * — ZREVRANGE WITHSCORES
     * */
    public StatisticsDto.RankingRes getTopStores() {
        String key = "sales:store:ranking:" + LocalDate.now();
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 4);
        return StatisticsDto.RankingRes.builder()
                .rankings(toRankingItems(tuples))
                .build();
    }

    /**
     * 3. 메뉴 판매 랭킹 TOP 5
     * — ZREVRANGE WITHSCORES
     * */
    public StatisticsDto.RankingRes getTopMenus() {
        String key = "sales:menu:ranking:" + LocalDate.now();
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 4);
        return StatisticsDto.RankingRes.builder()
                .rankings(toRankingItems(tuples))
                .build();
    }

    /**
     * 4. 시간대별 매출
     * — HGETALL
     * */
    public StatisticsDto.HourlySalesRes getHourlySales() {
        String key = "sales:hourly:" + LocalDate.now();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<StatisticsDto.HourlySalesItem> items = entries.entrySet().stream()
                .map(e -> StatisticsDto.HourlySalesItem.builder()
                        .hour(Integer.parseInt((String) e.getKey()))
                        .sales(Long.parseLong((String) e.getValue()))
                        .build())
                .sorted(Comparator.comparingInt(StatisticsDto.HourlySalesItem::getHour))
                .toList();
        return StatisticsDto.HourlySalesRes.builder().hourlyData(items).build();
    }

    /**
     * 5. 카테고리별 매출 비율
     * — HGETALL
     * */
    public StatisticsDto.RatioRes getCategoryRatio() {
        return toRatioRes("sales:category:" + LocalDate.now());
    }

    /**
     * 6. 결제수단별 매출 비율
     * — HGETALL
     * */
    public StatisticsDto.RatioRes getPaymentMethodRatio() {
        return toRatioRes("sales:payment:" + LocalDate.now());
    }

    // ─── private helpers ───────────────────────────────────

    /**
     * ZSET 의 TypedTuple → RankingItem 변환. member 는 "idx|name" 형식.
     * */
    private List<StatisticsDto.RankingItem> toRankingItems(Set<ZSetOperations.TypedTuple<String>> tuples) {
        if (tuples == null) return List.of();
        return tuples.stream()
                .map(tuple -> {
                    String member = tuple.getValue();
                    Double score = tuple.getScore();
                    String[] parts = member.split("\\|", 2);  // "idx|name"
                    return StatisticsDto.RankingItem.builder()
                            .idx(Long.parseLong(parts[0]))
                            .name(parts[1])
                            .score(score == null ? 0L : score.longValue())
                            .build();
                })
                .toList();
    }

    /**
     * Hash → RatioRes 변환 (카테고리/결제수단 공통).
     * */
    private StatisticsDto.RatioRes toRatioRes(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<StatisticsDto.RatioItem> items = entries.entrySet().stream()
                .map(e -> StatisticsDto.RatioItem.builder()
                        .name((String) e.getKey())
                        .amount(Long.parseLong((String) e.getValue()))
                        .build())
                .toList();
        return StatisticsDto.RatioRes.builder().ratios(items).build();
    }
}