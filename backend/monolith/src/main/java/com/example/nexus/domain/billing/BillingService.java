package com.example.nexus.domain.billing;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.common.enums.CardCompany;
import com.example.nexus.domain.billing.model.Billing;
import com.example.nexus.domain.billing.model.BillingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BillingService {

    private final BillingRepository billingRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${billing.secret-key}")
    private String secretKey;

    @Transactional
    public void issueBillingKey(Long storeIdx, BillingDto.IssueBillingKeyRequestDto request) {
        String basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        String customerKey = "STORE_" + storeIdx;

        try {
            Map<?, ?> responseBody = restClient.post()
                    .uri("https://api.tosspayments.com/v1/billing/authorizations/issue")
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "authKey", request.getAuthKey(),
                            "customerKey", customerKey
                    ))
                    .retrieve()
                    .body(Map.class);

            if (responseBody == null) {
                throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
            }

            // --- 1. 데이터 추출 및 매핑 (방어적 코드) ---
            String mId = getSafeString(responseBody, "mId");
            String resCustomerKey = getSafeString(responseBody, "customerKey");
            String authenticatedAt = getSafeString(responseBody, "authenticatedAt");
            String method = getSafeString(responseBody, "method");
            String billingKey = getSafeString(responseBody, "billingKey");

            String cardCompany = null;
            String cardNumber = null;

            // 카드 정보 추출
            Object cardObj = responseBody.get("card");
            if (cardObj instanceof Map) {
                Map<String, Object> cardInfo = (Map<String, Object>) cardObj;
                
                // issuerCode로 카드사 이름 찾기
                Object issuerCodeObj = cardInfo.get("issuerCode");
                String issuerCode = (issuerCodeObj != null) ? issuerCodeObj.toString() : "";
                cardCompany = CardCompany.getKorNameByCode(issuerCode);
                
                // 카드 번호 (토스가 마스킹해서 줌)
                Object numberObj = cardInfo.get("number");
                cardNumber = (numberObj != null) ? numberObj.toString() : null;
            }

            // --- 2. DB 저장 및 업데이트 ---
            final String finalCardCompany = cardCompany;
            final String finalCardNumber = cardNumber;

            billingRepository.findByStoreIdx(storeIdx).ifPresentOrElse(
                existingBilling -> existingBilling.updateBillingInfo(
                    mId, resCustomerKey, authenticatedAt, method, billingKey, finalCardCompany, finalCardNumber
                ),
                () -> {
                    Billing newBilling = Billing.builder()
                        .mId(mId)
                        .customerKey(resCustomerKey)
                        .authenticatedAt(authenticatedAt)
                        .method(method)
                        .billingKey(billingKey)
                        .storeIdx(storeIdx)
                        .cardCompany(finalCardCompany)
                        .cardNumber(finalCardNumber)
                        .build();
                    billingRepository.save(newBilling);
                }
            );

        } catch (Exception e) {
            throw new RuntimeException("빌링키 발급 중 오류 발생: " + e.getMessage());
        }
    }

    // 맵에서 안전하게 문자열 가져오기
    private String getSafeString(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return (val != null) ? val.toString() : "";
    }

    @Transactional(readOnly = true)
    public List<BillingDto.BillingResponseDto> getBillingListByStore(Long storeIdx) {
        return billingRepository.findByStoreIdx(storeIdx).stream()
                .map(BillingDto.BillingResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillingDto.BillingResponseDto> getBillingList() {
        return billingRepository.findAll().stream()
                .map(BillingDto.BillingResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBillingInfo(Long storeIdx) {
        billingRepository.deleteByStoreIdx(storeIdx);
    }
}
