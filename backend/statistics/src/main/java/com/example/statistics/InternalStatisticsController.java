package com.example.statistics;

import com.example.statistics.domain.daily.DailyDumpService;
import com.example.statistics.domain.daily.dto.DumpResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 운영 도구 + 개발 검증용 내부 API.
 * 자동 스케줄러 실패 시 수동 dump, 누락 복구 시 catch-up 등에 활용.
 *
 * ⚠️ 운영 환경에서는 K8s NetworkPolicy 또는 Spring Security 로 외부 접근 차단 필수.
 */
@Tag(name = "내부 운영 도구", description = "수동 dump / 운영 검증용. K8s NetworkPolicy 로 외부 접근 차단 권장 (#889).")
@RestController
@RequestMapping("/internal/statistics")
@RequiredArgsConstructor
public class InternalStatisticsController {
    private final DailyDumpService dailyDumpService;

    /**
     * 특정 날짜의 Redis 일별 집계를 MariaDB 에 수동 dump.
     *
     * @param date 처리할 날짜 (생략 시 어제)
     * @return DumpResult — status (SUCCESS/FAILED/SKIPPED), 처리 건수, 소요 시간 등
     */
    @Operation(
            summary = "수동 일별 dump",
            description = """
                    특정 날짜의 Redis 일별 집계를 MariaDB (daily_*_sales) 에 저장.

                    용도:
                    - 자동 스케줄러 (#889 새벽 5시) 실패 시 복구
                    - 누락된 날짜 catch-up
                    - 개발/검증 시 임의 시점 dump

                    응답: DumpResult { status, processed_count, duration_ms, ... }
                    """
    )
    @PostMapping("/dailyDump")
    public ResponseEntity<DumpResult> manualDailyDump(
            @Parameter(
                    description = "처리할 날짜 (yyyy-MM-dd 형식, 생략 시 어제)",
                    examples = {
                            @ExampleObject(name = "어제 (기본값)", value = "2026-05-26"),
                            @ExampleObject(name = "30일 전 (catch-up)", value = "2026-04-27"),
                            @ExampleObject(name = "오늘 (당일 - 보통 미사용)", value = "2026-05-27"),
                            @ExampleObject(name = "미래 (데이터 없음)", value = "2099-01-01")
                    }
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate target = (date != null) ? date : LocalDate.now().minusDays(1);
        DumpResult result = dailyDumpService.dump(target);
        return ResponseEntity.ok(result);
    }
}
