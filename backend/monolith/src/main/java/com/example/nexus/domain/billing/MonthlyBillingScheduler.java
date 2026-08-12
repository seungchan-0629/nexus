package com.example.nexus.domain.billing;

import com.example.nexus.domain.billing.model.Billing;
import com.example.nexus.domain.head.HeadIncomeRepository;
import com.example.nexus.domain.head.model.HeadIncome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonthlyBillingScheduler {
    private final BillingRepository billingRepository;
    private final HeadIncomeRepository headIncomeRepository;

    @Value("${billing.secret-key}")
    private String secretKey;

    /**
     * 매월 10일 새벽 2시에 지난 달 미결제 내역을 자동 결제합니다.
     * Cron: "0 0 2 10 * *" (초 분 시 일 월 요일)
     * 배치 도는지 확인 시 cron -> (fixedRate = 150000)로 15초 마다 돌게 설정
     */
    @Transactional
    @Scheduled(cron = "0 0 2 10 * *")
    public void executeMonthlyBilling() {
        log.info("정기 결제 스케줄러 실행 시작");

        // 1. 기간 설정: 전월 1일 00:00:00 ~ 당월 1일 00:00:00
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfLastMonth = now.withDayOfMonth(1).with(LocalTime.MIN);

        // 결제 상품명 생성 (예: 2026년 04월분 정산)
        String orderName = startOfLastMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월분 정산"));

        // 2. 등록된 빌링키 정보 전체 조회
        List<Billing> billingList = billingRepository.findAll();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // 토스페이먼츠 Basic Auth 헤더 (Secret Key + ":")
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (Billing target : billingList) {
            try {
                // 3. 해당 가맹점의 전월 미결제 내역(status=false) 조회
                // customerKey에 storeIdx가 저장되어 있다고 가정합니다.
                String customerKey = target.getCustomerKey();
                Long storeIdx = Long.parseLong(customerKey.replace("STORE_", ""));
                List<HeadIncome> unpaidIncomes = headIncomeRepository.findUnpaidByStoreAndMonth(
                        storeIdx, startOfLastMonth, endOfLastMonth);

                if (unpaidIncomes.isEmpty()) {
                    log.info("결제 대상 없음 - 가맹점 번호: {}", storeIdx);
                    continue;
                }

                // 4. 합계 금액 계산
                long totalAmount = unpaidIncomes.stream()
                        .mapToLong(HeadIncome::getPrice)
                        .sum();

                if (totalAmount <= 0) {
                    continue;
                }

                // 5. 토스페이먼츠 빌링 결제 요청 Body 생성
                Map<String, Object> body = new HashMap<>();
                body.put("customerKey", target.getCustomerKey());
                body.put("amount", totalAmount);
                body.put("orderId", "BILL-" + UUID.randomUUID().toString().substring(0, 20));
                body.put("orderName", orderName);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                String url = "https://api.tosspayments.com/v1/billing/" + target.getBillingKey();

                // 6. API 호출
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("결제 성공 - 가맹점: {}, 금액: {}, 주문명: {}", storeIdx, totalAmount, orderName);
                    
                    // 7. DB 상태 업데이트 (status = true)
                    for (HeadIncome income : unpaidIncomes) {
                        income.updateStatus(true);
                    }
                    headIncomeRepository.saveAll(unpaidIncomes);
                }

            } catch (Exception e) {
                log.error("결제 처리 중 오류 발생 - 가맹점(customerKey): {}, 사유: {}", target.getCustomerKey(), e.getMessage());
            }
        }
        log.info("정기 결제 스케줄러 실행 종료");
    }
}
