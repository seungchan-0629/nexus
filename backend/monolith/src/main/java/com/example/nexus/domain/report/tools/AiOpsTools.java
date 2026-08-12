package com.example.nexus.domain.report.tools;

import com.example.nexus.common.enums.DeliveryStatus;
import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.domain.delivery.DeliveryRepository;
import com.example.nexus.domain.head.HeadInventoryRepository;
import com.example.nexus.domain.wastelog.WasteLogRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

// 공급망/운영 리스크 데이터 조회 도구 (본사 재고 / 폐기 / 배송) — AI가 대화 맥락에 맞춰 호출
@Component
public class AiOpsTools {

    private final HeadInventoryRepository headInventoryRepository;
    private final WasteLogRepository wasteLogRepository;
    private final DeliveryRepository deliveryRepository;

    public AiOpsTools(HeadInventoryRepository headInventoryRepository,
                      WasteLogRepository wasteLogRepository,
                      DeliveryRepository deliveryRepository) {
        this.headInventoryRepository = headInventoryRepository;
        this.wasteLogRepository = wasteLogRepository;
        this.deliveryRepository = deliveryRepository;
    }

    // 본사 창고 재고 위험 현황 (상태별 품목 수)
    @Tool(description = "본사 창고 재고의 위험 현황을 조회한다. 유통기한 임박/만료, 재고 부족/위험 품목 수.")
    public Map<String, Object> getInventoryStatus() {
        return Map.<String, Object>of(
                "유통기한임박(EXPIRING)", headInventoryRepository.countByStatus(InventoryStatus.EXPIRING),
                "유통기한만료(EXPIRED)", headInventoryRepository.countByStatus(InventoryStatus.EXPIRED),
                "재고부족(LOW)", headInventoryRepository.countByStatus(InventoryStatus.LOW),
                "위험(CRITICAL)", headInventoryRepository.countByStatus(InventoryStatus.CRITICAL)
        );
    }

    // 기간별 카테고리별 폐기량
    @Tool(description = "지정한 기간의 카테고리별 폐기량을 조회한다. 날짜는 yyyy-MM-dd.")
    public List<Map<String, Object>> getWasteByCategory(
            @ToolParam(description = "시작일 yyyy-MM-dd") String startDate,
            @ToolParam(description = "종료일 yyyy-MM-dd") String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX); // BETWEEN 쿼리라 종료일 끝까지 포함
        return wasteLogRepository.findCategoryWasteRatio(start, end).stream()
                .map(r -> Map.<String, Object>of("category", r[0], "quantity", r[1]))
                .toList();
    }

    // 배송 상태별 건수
    @Tool(description = "현재 배송 현황을 상태별 건수로 조회한다. 준비/출발/배송중/완료/지연.")
    public Map<String, Object> getDeliveryStatus() {
        return Map.<String, Object>of(
                "준비(READY)", deliveryRepository.countByDeliveryStatus(DeliveryStatus.READY),
                "출발(START)", deliveryRepository.countByDeliveryStatus(DeliveryStatus.START),
                "배송중(DELIVERYING)", deliveryRepository.countByDeliveryStatus(DeliveryStatus.DELIVERYING),
                "완료(DELIVERED)", deliveryRepository.countByDeliveryStatus(DeliveryStatus.DELIVERED),
                "지연(DELAY)", deliveryRepository.countByDeliveryStatus(DeliveryStatus.DELAY)
        );
    }
}
