package com.example.nexus.domain.settlement.model;

import com.example.nexus.domain.head.model.HeadIncomeDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class SettlementDto {

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "결제 요청 데이터")
    public static class PayReq {
        @io.swagger.v3.oas.annotations.media.Schema(description = "총 결제 금액", example = "1500000")
        private Long paymentPrice;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 대상 본사 수입(HeadIncome) ID 목록", example = "[101, 102, 103]")
        List<Long> headIncomeidxList;
    }

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "결제 요청 응답 데이터")
    public static class PayRes {
        @io.swagger.v3.oas.annotations.media.Schema(description = "생성된 정산 ID", example = "50")
        private Long settlementIdx;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 금액", example = "1500000")
        private Long paymentPrice;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 처리된 수입 내역 ID 목록", example = "[101, 102, 103]")
        private List<Long> headIncomeIdxList;
    }

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "결제 검증 요청 데이터")
    public static class VerifyReq {
        @io.swagger.v3.oas.annotations.media.Schema(description = "포트원 결제 고유 번호 (payment_id)", example = "payment-88099881-8b38-42f0-9377-51253457a41d")
        private String paymentId;
    }

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "미납 내역 응답 데이터")
    public static class unPaid {
        @io.swagger.v3.oas.annotations.media.Schema(description = "미납된 수입 내역 상세 목록")
        private List<HeadIncomeDto.FindHeadIncomeRes> unpaidList;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "정산 ID", example = "50")
        private Long settlementIdx;
    }

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "최종 결제 실패 내역 응답")
    public static class FinalRetryFailRes {
        @io.swagger.v3.oas.annotations.media.Schema(description = "정산 ID", example = "50")
        private Long idx;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "가맹점 ID", example = "5")
        private Long storeIdx;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "총 미납 금액", example = "1500000")
        private Integer amount;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 대상 월 (yyyy-MM)", example = "2024-05")
        private String payedMonth;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "실패 사유", example = "잔액 부족")
        private String failReason;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "연관된 수입 내역 ID 목록", example = "[101, 102]")
        private List<Long> headIncomeIdxList;
    }
}
