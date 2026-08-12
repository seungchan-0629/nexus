package com.example.nexus.domain.statistics;

import com.example.nexus.common.model.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "통계 (모놀리식 잔존)",
        description = "#854 통계 라우팅 분리 후 모놀리식에 잔존하는 통계 API. 신규 호출은 통계 MSA(:8081/statistics/*) 사용 권장."
)
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    /**
     * 오늘 실시간 총 매출 조회
     */
    @Operation(
            summary = "오늘 총 매출 (잔존)",
            description = "모놀리식 DB 의 pos_pay SUM. 통계 MSA 의 Redis 기반과 동일 결과지만 응답 속도 느림."
    )
    @GetMapping("/sales/today")
    public ResponseEntity<BaseResponse> getTodaySales() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getTodaySales()));
    }

    /**
     * 매장 매출 랭킹 TOP 5 조회
     */
    @Operation(
            summary = "매장 매출 TOP 5 (잔존)",
            description = "모놀리식 DB GROUP BY 기반 매장별 매출 상위 5개."
    )
    @GetMapping("/store/top")
    public ResponseEntity<BaseResponse> getTopStores() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getTopStores()));
    }

    /**
     * 인기 메뉴 TOP 5 조회
     */
    @Operation(
            summary = "인기 메뉴 TOP 5 (잔존)",
            description = "모놀리식 DB GROUP BY 기반 메뉴별 판매량 상위 5개."
    )
    @GetMapping("/menu/top")
    public ResponseEntity<BaseResponse> getTopMenus() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getTopMenus()));
    }

    /**
     * 시간대별 매출 추이 조회 (0~23시)
     */
    @Operation(
            summary = "시간대별 매출 (잔존)",
            description = "모놀리식 DB GROUP BY HOUR(paid_at) 기반 0~23시 매출."
    )
    @GetMapping("/sales/hourly")
    public ResponseEntity<BaseResponse> getHourlySales() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getHourlySales()));
    }

    /**
     * 카테고리별 매출 비율 조회
     */
    @Operation(
            summary = "카테고리별 매출 (잔존)",
            description = "모놀리식 DB GROUP BY 기반 메뉴 카테고리별 매출."
    )
    @GetMapping("/category/ratio")
    public ResponseEntity<BaseResponse> getCategoryRatio() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getCategoryRatio()));
    }

    /**
     * 결제 수단별 매출 비율 조회
     */
    @Operation(
            summary = "결제 수단별 매출 (잔존)",
            description = "모놀리식 DB GROUP BY 기반 결제 수단별 매출."
    )
    @GetMapping("/payment/ratio")
    public ResponseEntity<BaseResponse> getPaymentMethodRatio() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getPaymentMethodRatio()));
    }
}
