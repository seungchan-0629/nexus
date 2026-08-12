package com.example.nexus.domain.billing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

public class BillingDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "빌링키 발급 요청")
    public static class IssueBillingKeyRequestDto {
        @Schema(description = "카드 인증 증명서")
        private String authKey;

        @Schema(description = "각 가맹점 구분 키", example = "STORE_1")
        private String customerKey;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingResponseDto {
        private Long id;
        private String mId;
        private String customerKey;
        private String authenticatedAt;
        private String method;
        private Long storeIdx;
        private String cardCompany;
        private String cardNumber;

        public static BillingResponseDto from(Billing billing) {
            return BillingResponseDto.builder()
                    .id(billing.getId())
                    .mId(billing.getMId())
                    .customerKey(billing.getCustomerKey())
                    .authenticatedAt(billing.getAuthenticatedAt())
                    .method(billing.getMethod())
                    .storeIdx(billing.getStoreIdx())
                    .cardCompany(billing.getCardCompany())
                    .cardNumber(billing.getCardNumber())
                    .build();
        }
    }
}
