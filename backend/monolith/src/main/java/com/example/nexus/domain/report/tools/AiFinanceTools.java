package com.example.nexus.domain.report.tools;

import com.example.nexus.domain.head.HeadIncomeRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 재무(본사 청구/정산) 데이터 조회 도구 — AI가 대화 맥락에 맞춰 호출
@Component
public class AiFinanceTools {

    private final HeadIncomeRepository headIncomeRepository;

    public AiFinanceTools(HeadIncomeRepository headIncomeRepository) {
        this.headIncomeRepository = headIncomeRepository;
    }

    // 특정 월 본사 전체 청구 총액
    @Tool(description = "지정한 연/월의 본사 전체 청구 총액(원)을 조회한다.")
    public long getBillingByMonth(
            @ToolParam(description = "연도 예: 2026") int year,
            @ToolParam(description = "월 1~12") int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1); // 쿼리가 반개방(< end)이라 다음 달 1일 0시
        Long sum = headIncomeRepository.sumTotalBillingByMonth(null, start, end);
        return sum != null ? sum : 0L;
    }

    // 특정 월 매장별 정산 현황 (발주건수/총청구액/정산완료여부)
    @Tool(description = "지정한 연/월의 매장별 정산 현황을 조회한다. 각 매장의 발주 건수, 총 청구액, 정산 완료 여부.")
    public List<Map<String, Object>> getStoreSettlements(
            @ToolParam(description = "연도 예: 2026") int year,
            @ToolParam(description = "월 1~12") int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        // findStoreSettlementByMonth → Object[]{ idx, storeName, count, sumPrice, paid(boolean) }
        return headIncomeRepository.findStoreSettlementByMonth(null, start, end, PageRequest.of(0, 100))
                .getContent().stream()
                .map(r -> Map.<String, Object>of(
                        "storeName", r[1],
                        "orderCount", r[2],
                        "totalAmount", r[3],
                        "paid", r[4]))
                .toList();
    }
}
