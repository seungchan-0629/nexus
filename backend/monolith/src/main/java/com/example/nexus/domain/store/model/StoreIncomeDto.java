package com.example.nexus.domain.store.model;

import com.example.nexus.common.model.PageResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class StoreIncomeDto {

    // 가맹점 월별 매출 현황 응답
    @Getter
    @Builder
    public static class MonthlyIncomeRes {
        private Long storeIdx;
        private String storeName;
        private List<MonthlyIncomeItemRes> monthlyIncomes;
    }

    @Getter
    @Builder
    public static class MonthlyIncomeItemRes {
        private int year;
        private int month;
        private Long totalAmount;
    }

    // 결제 내역 단건 DTO
    @Getter
    @Builder
    public static class PayHistoryRes {
        private Long posPayIdx;
        private String menuNames;
        private int payCount;
        private LocalDate paidDate;
        private Long payAmount;
    }

    // 상단 총액 + 하단 페이징 리스트
    @Getter
    @Builder
    public static class TotalSettlementRes {
        private Long monthlyTotalAmount;
        private PageResponse<PayHistoryRes> payHistory;
    }
}