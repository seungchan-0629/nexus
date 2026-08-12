package com.example.nexus.domain.orders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdersScheduler {
    private final OrdersService ordersService;

    // 매일 자정에 확정 발주 전체 승인
    // CronJob → batch-service:8090 으로 대체되어 비활성화
    // @Scheduled(cron = "0 0 0 * * *")
    public void autoApproveConfirmedOrders() {
        log.info("확정 발주 자동 전체 승인 스케줄러 실행");
        ordersService.approveAllConfirmed();
        log.info("확정 발주 자동 전체 승인 완료");
    }
}
