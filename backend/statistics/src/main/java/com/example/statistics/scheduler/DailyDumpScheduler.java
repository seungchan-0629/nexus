package com.example.statistics.scheduler;

import com.example.statistics.domain.daily.DailyDumpLogRepository;
import com.example.statistics.domain.daily.DailyDumpService;
import com.example.statistics.domain.daily.dto.DumpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 일별 dump 자동 스케줄러.
 * 매일 새벽 5시 (KST) 어제 데이터를 dump.
 * Multi-replica 환경에서 ShedLock 으로 한 인스턴스만 실행.
 *
 * <p>누락 복구 (catch-up) 로직:
 * <ul>
 *   <li>마지막 SUCCESS dump 날짜 조회</li>
 *   <li>(마지막 SUCCESS + 1) ~ 어제 까지 순차적으로 dump 호출</li>
 *   <li>실패한 날짜는 다음 cron 에서 재시도 (Redis TTL 7일 안이면 복구 가능)</li>
 *   <li>처음 한 번도 SUCCESS 없으면 어제만 dump</li>
 * </ul>
 *
 * <p>각 호출은 {@link DailyDumpService#dump(LocalDate)} 가 자체 멱등성 가드를 가지므로
 * 같은 날짜에 두 번 호출돼도 두 번째는 SKIPPED 반환.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyDumpScheduler {

    private final DailyDumpService dailyDumpService;
    private final DailyDumpLogRepository dumpLogRepository;

    /**
     * 매일 새벽 5시 (KST) 자동 트리거.
     * cron 형식: "초 분 시 일 월 요일" (Spring 6자리)
     *
     * <p>ShedLock 으로 multi-replica 환경에서 한 pod 만 실행.
     * - lockAtMostFor = 10m : 락 자동 해제 (deadlock 방지)
     * - lockAtLeastFor = 5m : 너무 빨리 끝나도 5분간 락 유지 (중복 실행 방지)
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @SchedulerLock(
            name = "dailyAggregateDump",
            lockAtMostFor = "10m",
            lockAtLeastFor = "5m"
    )
    public void runDailyDump() {
        log.info("[DailyDumpScheduler] start");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 1) catch-up 시작점: 마지막 SUCCESS + 1, 없으면 어제부터
        LocalDate startDate = dumpLogRepository.findLastSuccessfulDumpDate()
                .map(last -> last.plusDays(1))
                .orElse(yesterday);

        // 2) 이미 어제까지 처리됐으면 skip
        if (startDate.isAfter(yesterday)) {
            log.info("[DailyDumpScheduler] 이미 어제까지 dump 완료, skip (startDate={})", startDate);
            return;
        }

        int totalDays = (int) (yesterday.toEpochDay() - startDate.toEpochDay()) + 1;
        log.info("[DailyDumpScheduler] catch-up 범위: {} ~ {} ({}일)",
                startDate, yesterday, totalDays);

        // 3) 누락분 catch-up
        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        for (LocalDate date = startDate; !date.isAfter(yesterday); date = date.plusDays(1)) {
            try {
                DumpResult result = dailyDumpService.dump(date);
                int recordCount = result.getTotalSalesCount()
                        + result.getStoreSalesCount()
                        + result.getMenuSalesCount()
                        + result.getHourlySalesCount()
                        + result.getCategorySalesCount()
                        + result.getPaymentSalesCount();

                log.info("[DailyDumpScheduler] {} → {} (records={}, elapsed={}ms)",
                        date, result.getStatus(), recordCount, result.getSpendTime());

                switch (result.getStatus()) {
                    case SUCCESS -> successCount++;
                    case SKIPPED -> skippedCount++;
                    case FAILED -> failedCount++;
                }
            } catch (Exception e) {
                // dailyDumpService 가 자체 예외 처리 (recordFailure REQUIRES_NEW) 하지만,
                // 혹시 그 외 예외 발생 시에도 다른 날짜 처리 계속 진행하도록 안전망.
                failedCount++;
                log.error("[DailyDumpScheduler] {} 처리 중 예외 - 다음 날짜 계속 진행", date, e);
            }
        }

        log.info("[DailyDumpScheduler] complete (success={}, skipped={}, failed={})",
                successCount, skippedCount, failedCount);
    }
}
