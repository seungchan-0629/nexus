package com.example.nexus.domain.report.tools;

import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.StoreService;
import com.example.nexus.domain.store.model.StoreDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 가맹점(Store) 데이터 조회 도구 — AI가 대화 맥락에 맞춰 호출
@Component
public class AiStoreTools {

    private final StoreRepository storeRepository;
    private final StoreService storeService;   // 입폐점 추이 로직 재사용

    public AiStoreTools(StoreRepository storeRepository, StoreService storeService) {
        this.storeRepository = storeRepository;
        this.storeService = storeService;
    }

    // 현재 가맹점 수 (전체/입점/폐점)
    @Tool(description = "현재 가맹점 수를 조회한다. 전체, 입점(운영 중), 폐점 수.")
    public Map<String, Object> getStoreCount() {
        long active = storeRepository.countByIsDeletedFalse();
        long closed = storeRepository.countByIsDeletedTrue();
        return Map.<String, Object>of("전체", active + closed, "입점", active, "폐점", closed);
    }

    // 지정 연도 월별 입점/폐점 추이 (기존 StoreService 로직 재사용)
    @Tool(description = "지정한 연도의 월별 가맹점 입점/폐점 추이를 조회한다.")
    public List<Map<String, Object>> getStoreOpenCloseTrend(
            @ToolParam(description = "연도 예: 2026") int year) {
        StoreDto.MonthlyTrendRes t = storeService.getMonthlyTrend(year);
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < t.getLabels().size(); i++) {
            out.add(Map.of(
                    "month", t.getLabels().get(i),
                    "opened", t.getOpenedCounts().get(i),
                    "closed", t.getClosedCounts().get(i)));
        }
        return out;
    }
}
