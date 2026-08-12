package com.example.nexus.domain.report.tools;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.domain.orders.OrdersRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

// 발주(Orders) 도메인 데이터 조회 도구 — AI가 대화 맥락에 맞춰 호출
@Component
public class AiOrderTools {

    private final OrdersRepository ordersRepository;

    public AiOrderTools(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    // 현재 발주 현황(건수) 조회
    @Tool(description = "현재 발주 현황을 조회한다. 이상(위험) 발주 수, 확정 대기 수, 확정 수, 승인 수.")
    public Map<String, Object> getOrderStats() {
        return Map.<String, Object>of(
                "이상발주", ordersRepository.countByIsDangerTrue(),
                "확정대기(WAITING)", ordersRepository.countByOrdersStatus(OrdersStatus.WAITING),
                "확정(CONFIRMED)", ordersRepository.countByOrdersStatus(OrdersStatus.CONFIRMED),
                "승인(APPROVE)", ordersRepository.countByOrdersStatus(OrdersStatus.APPROVE)
        );
    }
}
