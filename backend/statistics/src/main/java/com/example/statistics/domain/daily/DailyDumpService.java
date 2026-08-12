package com.example.statistics.domain.daily;

import com.example.statistics.domain.daily.dto.DumpResult;
import com.example.statistics.domain.daily.model.DumpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyDumpService {

    private final DailyDumpInternalService internalService;
    private final DailyDumpLogRepository dailyDumpLogRepository;

    /**
     * 특정 날짜의 Redis 일별 집계를 MariaDB 에 dump.
     * 이미 SUCCESS 로 dump 됐으면 skip (멱등성).
     */
    public DumpResult dump(LocalDate targetDate) {
        long start = System.currentTimeMillis();

        // 1. 멱등성 체크
        if(dailyDumpLogRepository.existsByAggregateDateAndDumpStatus(targetDate, DumpStatus.SUCCESS)) {
            log.info("[DailyDump] already done, skip: {}", targetDate);
            return DumpResult.skipped(targetDate);
        }

        try {
            // 2. 트랜잭션 안에서 Redis 읽기 + MariaDB INSERT + SUCCESS 로그
            return internalService.doDump(targetDate, start);
        } catch (Exception e) {
            // 3. 실패 시 별도 트랜잭션으로 FAILED 로그
            long elapsed = System.currentTimeMillis() - start;
            internalService.recordFailure(targetDate, e.getMessage());
            log.error("[DailyDump] failed: {}", targetDate, e);
            return DumpResult.failed(targetDate, e.getMessage(), elapsed);
        }
    }

}
