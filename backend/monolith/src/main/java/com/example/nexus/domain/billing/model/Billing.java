package com.example.nexus.domain.billing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mId; // 상점 ID
    private String customerKey; // 구매자 ID
    private String authenticatedAt; // 등록 시간
    private String method; // 결제 수단
    private String billingKey;
    private Long storeIdx;
    
    private String cardCompany; // 카드사 이름 (예: 신한카드)
    private String cardNumber;  // 마스킹된 카드번호 (예: 4518********1234)

    public void updateBillingInfo(String mId, String customerKey, String authenticatedAt, String method, String billingKey, String cardCompany, String cardNumber) {
        this.mId = mId;
        this.customerKey = customerKey;
        this.authenticatedAt = authenticatedAt;
        this.method = method;
        this.billingKey = billingKey;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
    }
}
