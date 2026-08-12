package org.example.spring.billingbatch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.spring.billingbatch.model.AfterBilling;
import org.example.spring.billingbatch.model.FailedPayment;
import org.example.spring.billingbatch.repository.AfterBillingRepository;
import org.example.spring.billingbatch.repository.FailedPaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Failed Payment", description = "결제 실패 내역 조회 API")
@RestController
@RequestMapping("/api/billing/failed")
@RequiredArgsConstructor
public class FailedPaymentController {

    private final FailedPaymentRepository failedPaymentRepository;
    private final AfterBillingRepository afterBillingRepository;

    @Operation(summary = "1차 결제 실패 내역 조회", description = "특정 월의 1차 정기결제 실패 데이터를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<FailedPayment>> getFailedPayments(
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", example = "2024-05")
            @RequestParam String month) {
        List<FailedPayment> failedPayments = failedPaymentRepository.findByPayedMonth(month);
        return ResponseEntity.ok(failedPayments);
    }

    @Operation(summary = "최종 결제 실패 내역 조회", description = "재시도(2차)까지 모두 실패한 데이터를 조회합니다.")
    @GetMapping("/final")
    public ResponseEntity<List<AfterBilling>> getFinalFailedPayments(
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", example = "2024-05")
            @RequestParam String month) {
        List<AfterBilling> finalFailures = afterBillingRepository.findByPayedMonthAndIsRetryFailedTrue(month);
        return ResponseEntity.ok(finalFailures);
    }
}
