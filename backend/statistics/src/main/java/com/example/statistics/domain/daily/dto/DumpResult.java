package com.example.statistics.domain.daily.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dump 작업 결과.
 * 수동 트리거 API 응답 + 스케줄러 결과 로그 용도.
 */
@Getter
@Builder
public class DumpResult {

    /** SUCCESS / FAILED / SKIPPED */
    private final ResultStatus status;
    /** 처리한 집계 날짜 */
    private final LocalDate aggregateDate;
    /** 처리된 row 수 (테이블별) — 실패/skipped 시 0 */
    private final int totalSalesCount;
    private final int storeSalesCount;
    private final int menuSalesCount;
    private final int hourlySalesCount;
    private final int categorySalesCount;
    private final int paymentSalesCount;
    /** 소요 시간 (ms) */
    private final long spendTime;
    /** 실패 시 메시지 */
    private final String errorMessage;
    /** 완료 시각 */
    private final LocalDateTime completedAt;

    /** 결과 enum */
    public enum ResultStatus {
        SUCCESS, FAILED, SKIPPED
    }

    public static DumpResult success(LocalDate date, int total, int store, int menu, int hourly, int category, int payment, long elapsed) {
        return DumpResult.builder()
                .status(ResultStatus.SUCCESS)
                .aggregateDate(date)
                .totalSalesCount(total)
                .storeSalesCount(store)
                .menuSalesCount(menu)
                .hourlySalesCount(hourly)
                .categorySalesCount(category)
                .paymentSalesCount(payment)
                .spendTime(elapsed)
                .completedAt(LocalDateTime.now())
                .build();
    }

    public static DumpResult failed(LocalDate date, String error, long elapsed) {
        return DumpResult.builder()
                .status(ResultStatus.FAILED)
                .aggregateDate(date)
                .errorMessage(error)
                .spendTime(elapsed)
                .completedAt(LocalDateTime.now())
                .build();
    }

    public static DumpResult skipped(LocalDate date) {
        return DumpResult.builder()
                .status(ResultStatus.SKIPPED)
                .aggregateDate(date)
                .completedAt(LocalDateTime.now())
                .build();
    }
}