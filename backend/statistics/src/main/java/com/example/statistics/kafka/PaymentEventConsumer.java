package com.example.statistics.kafka;

import com.example.statistics.event.KafkaTopics;
import com.example.statistics.event.PaymentEvent;
import com.example.statistics.event.PaymentEventItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final StringRedisTemplate redisTemplate;

    @KafkaListener(topics = KafkaTopics.POS_PAYMENT_CREATED, concurrency = "3")
    public void onPaymentCreated(PaymentEvent event) {
        log.info("[pos.payment.created] received: posPayIdx={}, storeIdx={}, payAmount={}, items={}",
                event.posPayIdx(), event.storeIdx(), event.payAmount(), event.items().size());

        // 멱등성 : Redis SETNX 기반 (기존 existedById DB 조회 -> Redis O(1) 체크)
        String processdKey = "processed:" + event.posPayIdx();
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(processdKey, "1", Duration.ofDays(7));

        if(Boolean.FALSE.equals(isFirst)) {
            log.warn("[pos.payment.created] duplicate payment skipped: posPayIdx={}", event.posPayIdx());
            return;
        }

        // Redis 사전 집계 (DB INSERT 다음 호출)
        aggregateToRedis(event);
    }

    /**
     * 결제 이벤트로부터 6개 통계 키에 사전 집계.
     *
     * - sales:today:DATE          (String, INCRBY)
     * - sales:store:ranking:DATE  (Sorted Set, ZINCRBY)
     * - sales:menu:ranking:DATE   (Sorted Set, ZINCRBY)
     * - sales:hourly:DATE         (Hash, HINCRBY)
     * - sales:category:DATE       (Hash, HINCRBY)
     * - sales:payment:DATE        (Hash, HINCRBY)
     *
     * 각 키에 7일 TTL 설정 (Redis 메모리 관리).
     */
    private void aggregateToRedis(PaymentEvent event) {
        String date = event.paidAt().toLocalDate().toString();   // "2026-05-21"
        int hour = event.paidAt().getHour();
        long ttlSeconds = Duration.ofDays(7).getSeconds();

        String todayKey      = "sales:today:" + date;
        String storeRankKey  = "sales:store:ranking:" + date;
        String hourlyKey     = "sales:hourly:" + date;
        String paymentKey    = "sales:payment:" + date;
        String menuRankKey   = "sales:menu:ranking:" + date;
        String categoryKey   = "sales:category:" + date;

        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            StringRedisConnection conn = (StringRedisConnection) connection;

            // 1. 오늘 총 매출
            conn.incrBy(todayKey, event.payAmount());

            // 2. 매장 매출 랭킹
            conn.zIncrBy(storeRankKey, event.payAmount(),
                    event.storeIdx() + "|" + event.storeName());

            // 3. 시간대별 매출
            conn.hIncrBy(hourlyKey, String.valueOf(hour), event.payAmount());

            // 4. 결제수단별 매출
            conn.hIncrBy(paymentKey, event.method(), event.payAmount());

            // 5, 6. 메뉴 판매 랭킹 + 카테고리별 매출 (items 순회)
            for (PaymentEventItem item : event.items()) {
                conn.zIncrBy(menuRankKey, item.quantity(),
                        item.menuIdx() + "|" + item.menuName());
                conn.hIncrBy(categoryKey, item.menuCategoryName(), item.lineAmount());
            }

            // TTL — 6개 키 모두 같은 RTT 안에서 묶음
            conn.expire(todayKey,     ttlSeconds);
            conn.expire(storeRankKey, ttlSeconds);
            conn.expire(hourlyKey,    ttlSeconds);
            conn.expire(paymentKey,   ttlSeconds);
            conn.expire(menuRankKey,  ttlSeconds);
            conn.expire(categoryKey,  ttlSeconds);

            return null; // Pipeline 결과 미사용
        });

    }
}