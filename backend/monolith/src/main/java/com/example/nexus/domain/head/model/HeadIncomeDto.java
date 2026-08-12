package com.example.nexus.domain.head.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class HeadIncomeDto {

    @Builder
    @Getter
    public static class UnpaidRes {
        private Long idx;
        private Boolean status;
        private Long price;
        private Long storeIdx;
    }

    @Getter
    public static class FindHeadIncomeReq {
        private Long orderIdx;
    }

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "본사 수입 내역 조회 응답 데이터")
    public static class FindHeadIncomeRes {
        @io.swagger.v3.oas.annotations.media.Schema(description = "수입 내역 ID", example = "101")
        private Long idx;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "금액", example = "500000")
        private Long price;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "납부 여부", example = "false")
        private boolean paid;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "가맹점 ID", example = "5")
        private Long storeIdx;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "연관 주문 ID", example = "20240528001")
        private Long ordersIdx;
    }

    @Getter
    @Builder
    public static class ListRes {
        private Long headIncomeIdx;
        private Long storeIdx;
        private String storeName;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private int orderCount;
        private Long totalPrice;
        private Boolean status;
        private Long settlementIdx;
    }

    // 본사 정산 관리 - 가맹점별 정산 요약 단건
    @Getter
    @Builder
    public static class StoreSettlementRes {
        private Long storeIdx;
        private String storeName;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private int orderCount;
        private Long totalPrice;
        private Boolean status;
    }

    // 본사 정산 관리 - 전체 응답 (상단 요약 카드 + 페이징 리스트)
    @Getter
    @Builder
    public static class HeadSettlementSummaryRes {
        private Long totalBillingAmount;
        private int totalStoreCount;
    }
}
