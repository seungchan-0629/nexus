package com.example.batch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Tag(name = "발주 배치 작업", description = "확정 발주 일괄 승인 배치 잡 트리거. 동시 실행 방지(AtomicBoolean) 포함.")
@Slf4j
@RestController
@RequestMapping("/batch/jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job approveConfirmedOrdersJob;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Operation(
            summary = "발주 일괄 승인 배치 실행",
            description = """
                    확정(CONFIRMED) 상태의 발주를 일괄 승인하는 Spring Batch 잡을 즉시 실행.

                    동작:
                    - 이미 실행 중이면 409 Conflict 반환 (중복 실행 방지)
                    - 파티션 구조(product 단위)로 병렬 처리
                    - 완료 후 HeadInventory 차감, Delivery 생성, StoreInventory 적재

                    ⚠️ monolith의 PUT /orders/confirmed/approve 가 이 엔드포인트를 RestTemplate 으로 호출함.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배치 잡 정상 시작"),
            @ApiResponse(responseCode = "409", description = "이미 실행 중인 배치 잡 존재"),
            @ApiResponse(responseCode = "500", description = "배치 잡 실행 실패")
    })
    @PostMapping("/approve")
    public ResponseEntity<String> triggerApprove() {
        if (!running.compareAndSet(false, true)) {
            return ResponseEntity.status(409).body("batch job already running");
        }
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime("runAt", LocalDateTime.now())
                    .toJobParameters();

            log.info("발주 일괄승인 배치 시작 runAt={}", LocalDateTime.now());
            jobLauncher.run(approveConfirmedOrdersJob, params);
            log.info("발주 일괄승인 배치 완료");

            return ResponseEntity.ok("batch job started");
        } catch (Exception e) {
            log.error("발주 일괄승인 배치 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("batch job failed: " + e.getMessage());
        } finally {
            running.set(false);
        }
    }
}
