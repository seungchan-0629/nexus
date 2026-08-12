package com.example.nexus.domain.dashboard;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.dashboard.model.DashboardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "본사 대시보드", description = "본사 운영 KPI + 현황 리스트 (가맹점/발주/재고/배송)")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * 가맹점 현황 KPI 조회
     */
    @Operation(
            summary = "가맹점 현황 KPI",
            description = "총 매장 수, 이번 달 신규 매장 수, 전일 대비 증감률."
    )
    @GetMapping("/store/kpi")
    public ResponseEntity<BaseResponse> getStoreKpi() {
        DashboardDto.StoreKpiRes result = dashboardService.getStoreKpi();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 발주 현황 KPI 조회
     */
    @Operation(
            summary = "발주 현황 KPI",
            description = "금일 자동 발주 건수, 확정 발주 건수, 금일 수동 발주 건수."
    )
    @GetMapping("/orders/kpi")
    public ResponseEntity<BaseResponse> getOrdersKpi() {
        DashboardDto.OrdersKpiRes result = dashboardService.getOrdersKpi();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 재고 위험 KPI 조회
     */
    @Operation(
            summary = "재고 위험 KPI",
            description = "LOW(주의) / CRITICAL(위험) 상태 본사 재고 품목 수."
    )
    @GetMapping("/inventory/kpi")
    public ResponseEntity<BaseResponse> getInventoryKpi() {
        DashboardDto.InventoryKpiRes result = dashboardService.getInventoryKpi();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 배송 현황 KPI 조회
     */
    @Operation(
            summary = "배송 현황 KPI",
            description = "진행중 배송 건수 + 지연 배송 목록."
    )
    @GetMapping("/delivery/kpi")
    public ResponseEntity<BaseResponse> getDeliveryKpi() {
        DashboardDto.DeliveryKpiRes result = dashboardService.getDeliveryKpi();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 주간 발주 통계 조회
     */
    @Operation(
            summary = "주간 발주 통계",
            description = "최근 7일 일별 자동/수동 발주 건수 (차트용)."
    )
    @GetMapping("/orders/weekly/stats")
    public ResponseEntity<BaseResponse> getWeeklyOrderStats() {
        DashboardDto.WeeklyOrderStatsRes result = dashboardService.getWeeklyOrderStats();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 위험 재고 목록 조회
     */
    @Operation(
            summary = "위험 재고 목록",
            description = "LOW/CRITICAL 상태 본사 재고 목록 (Slice 페이징)."
    )
    @GetMapping("/inventory/danger")
    public ResponseEntity<BaseResponse> getDangerInventoryList(
            @Parameter(description = "페이지 번호 (0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "4")
            @RequestParam(defaultValue = "4") int size) {
        DashboardDto.DangerInventoryRes result = dashboardService.getDangerInventoryList(page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 이상 발주 통계 조회
     */
    @Operation(
            summary = "이상 발주 통계",
            description = "최근 6개월 월별 이상 발주 상태별 건수 (차트용)."
    )
    @GetMapping("/orders/danger/stats")
    public ResponseEntity<BaseResponse> getOrdersDangerStats() {
        DashboardDto.DangerStatsRes result = dashboardService.getOrdersDangerStats();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 지연 배송 목록 조회
     */
    @Operation(
            summary = "지연 배송 목록",
            description = "지연 상태 배송 목록 (Slice 페이징)."
    )
    @GetMapping("/delivery/delay")
    public ResponseEntity<BaseResponse> getDelayDeliveryList(
            @Parameter(description = "페이지 번호 (0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "4")
            @RequestParam(defaultValue = "4") int size) {
        DashboardDto.DelayDeliveryRes result = dashboardService.getDelayDeliveryList(page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 배송 비율 조회
     */
    @Operation(
            summary = "배송 상태 비율",
            description = "배송 상태별 건수 (차트용)."
    )
    @GetMapping("/delivery/ratio")
    public ResponseEntity<BaseResponse> getDeliveryRatio() {
        DashboardDto.DeliveryRatioRes result = dashboardService.getDeliveryRatio();
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
