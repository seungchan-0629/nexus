package com.example.nexus.domain.report.tools;

import com.example.nexus.domain.pos.PosOrdersItemRepository;
import com.example.nexus.domain.pos.PosPayRepository;
import com.example.nexus.domain.statistics.StatisticsRepository;
import com.example.nexus.domain.store.StoreRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

// AI(LLM)가 대화 맥락에 맞춰 직접 호출하는 데이터 조회 도구 모음 (전 지점 통합 기준)
// 컨트롤러가 아니라, ChatClient.tools(...)로 등록되어 AI가 필요 시 호출하는 함수 집합
@Component
public class AiStatsTools {

    private final PosPayRepository posPayRepository;
    private final PosOrdersItemRepository posOrdersItemRepository;
    private final StatisticsRepository statisticsRepository;
    private final StoreRepository storeRepository;

    public AiStatsTools(PosPayRepository posPayRepository,
                        PosOrdersItemRepository posOrdersItemRepository,
                        StatisticsRepository statisticsRepository,
                        StoreRepository storeRepository) {
        this.posPayRepository = posPayRepository;
        this.posOrdersItemRepository = posOrdersItemRepository;
        this.statisticsRepository = statisticsRepository;
        this.storeRepository = storeRepository;
    }

    // 지정 기간 전 지점 통합 총매출액
    @Tool(description = "지정한 기간의 전 지점 통합 총매출액(원)을 조회한다. 날짜는 yyyy-MM-dd 형식.")
    public long getSalesByPeriod(
            @ToolParam(description = "조회 시작일 yyyy-MM-dd") String startDate,
            @ToolParam(description = "조회 종료일 yyyy-MM-dd") String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        Long sum = posPayRepository.sumSalesByDateBetween(start, end);
        return sum != null ? sum : 0L;
    }

    // 지정 기간 전 지점 통합 인기 메뉴 순위
    @Tool(description = "지정한 기간 전 지점 통합 인기 메뉴를 순위대로 조회한다.")
    public List<String> getTopProducts(
            @ToolParam(description = "시작일 yyyy-MM-dd") String startDate,
            @ToolParam(description = "종료일 yyyy-MM-dd") String endDate,
            @ToolParam(description = "가져올 순위 개수(기본 5)", required = false) Integer limit) {
        int n = (limit != null && limit > 0) ? limit : 5;
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        return posOrdersItemRepository.findTopSellingMenus(start, end, PageRequest.of(0, n));
    }

    // 지정 기간 매장별 매출 랭킹
    @Tool(description = "지정한 기간 매장별 매출 랭킹 TOP N을 조회한다. 각 항목은 매장명과 매출액.")
    public List<Map<String, Object>> getTopStores(
            @ToolParam(description = "시작일 yyyy-MM-dd") String startDate,
            @ToolParam(description = "종료일 yyyy-MM-dd") String endDate,
            @ToolParam(description = "가져올 매장 수(기본 5)", required = false) Integer limit) {
        int n = (limit != null && limit > 0) ? limit : 5;
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        // findTopStoresByPeriod → Object[]{ storeIdx, storeName, sumSales }
        return posPayRepository.findTopStoresByPeriod(start, end, PageRequest.of(0, n)).stream()
                .map(r -> Map.<String, Object>of("storeName", r[1], "sales", r[2]))
                .toList();
    }

    // 특정 날짜의 시간대별 매출
    @Tool(description = "특정 날짜의 시간대별(0~23시) 전 지점 통합 매출을 조회한다. 날짜는 yyyy-MM-dd.")
    public List<Map<String, Object>> getHourlySales(
            @ToolParam(description = "조회 날짜 yyyy-MM-dd") String date) {
        LocalDateTime start = LocalDate.parse(date).atStartOfDay();
        LocalDateTime end = start.plusDays(1); // 쿼리가 반개방(< end)이라 다음 날 0시까지
        return statisticsRepository.findHourlySales(start, end).stream()
                .map(r -> Map.<String, Object>of("hour", r[0], "sales", r[1]))
                .toList();
    }

    // 기간별 카테고리별 매출
    @Tool(description = "지정한 기간의 메뉴 카테고리별 전 지점 통합 매출을 조회한다. 날짜는 yyyy-MM-dd.")
    public List<Map<String, Object>> getSalesByCategory(
            @ToolParam(description = "시작일 yyyy-MM-dd") String startDate,
            @ToolParam(description = "종료일 yyyy-MM-dd") String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(); // 종료일 포함
        return statisticsRepository.findSalesByCategory(start, end).stream()
                .map(r -> Map.<String, Object>of("category", r[0], "sales", r[1]))
                .toList();
    }

    // 특정 매장의 기간 매출
    @Tool(description = "특정 매장의 지정 기간 매출을 조회한다. 매장명을 정확히 전달. 날짜는 yyyy-MM-dd.")
    public Map<String, Object> getStoreSalesByPeriod(
            @ToolParam(description = "매장명") String storeName,
            @ToolParam(description = "시작일 yyyy-MM-dd") String startDate,
            @ToolParam(description = "종료일 yyyy-MM-dd") String endDate) {
        return storeRepository.findByStoreName(storeName)
                .map(store -> {
                    LocalDateTime from = LocalDate.parse(startDate).atStartOfDay();
                    LocalDateTime to = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
                    long sales = posPayRepository.sumPayAmountByStoreAndPeriod(store.getIdx(), from, to);
                    return Map.<String, Object>of("storeName", storeName, "sales", sales);
                })
                .orElse(Map.of("error", "해당 이름의 매장을 찾을 수 없습니다", "storeName", storeName));
    }
}
