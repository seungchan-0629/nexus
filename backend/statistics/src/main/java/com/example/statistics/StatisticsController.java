package com.example.statistics;

import com.example.statistics.common.model.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "실시간 통계", description = "Redis 사전 집계 기반 오늘 결제 데이터 통계 (#855, #868, #870)")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    /**
     * 오늘 실시간 총 매출 조회
     *
     * @return ResponseEntity 오늘 전체 매출 금액
     */
    @Operation(
            summary = "오늘 총 매출",
            description = "Redis 의 `sales:today:DATE` 키 (INCRBY 누적) 조회. O(1)."
    )
    @GetMapping("/sales/today")
    public ResponseEntity<BaseResponse> getTodaySales() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getTodaySales()));
    }

    /**
     * 매장 매출 랭킹 TOP 5 조회
     *
     * @return ResponseEntity 매출 상위 매장 5개 (매장명, 매출액)
     */
    @Operation(
            summary = "매장 매출 TOP 5",
            description = "Redis `sales:store:ranking:DATE` (ZSet) 의 ZREVRANGE 상위 5개 (member: `idx|name`)."
    )
    @GetMapping("/store/top")
    public ResponseEntity<BaseResponse> getTopStores() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getTopStores()));
    }

    /**
     * 인기 메뉴 TOP 5 조회
     *
     * @return ResponseEntity 판매 수량 상위 메뉴 5개 (메뉴명, 판매 수량)
     */
    @Operation(
            summary = "인기 메뉴 TOP 5",
            description = "Redis `sales:menu:ranking:DATE` (ZSet) 의 ZREVRANGE 상위 5개 (판매 수량 기준)."
    )
    @GetMapping("/menu/top")
    public ResponseEntity<BaseResponse> getTopMenus() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getTopMenus()));
    }

    /**
     * 시간대별 매출 추이 조회 (0~23시)
     *
     * @return ResponseEntity 시간대별 매출 금액 리스트
     */
    @Operation(
            summary = "시간대별 매출 추이",
            description = "Redis `sales:hourly:DATE` (Hash) 의 HGETALL 결과 (0~23시 별 매출액)."
    )
    @GetMapping("/sales/hourly")
    public ResponseEntity<BaseResponse> getHourlySales() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getHourlySales()));
    }

    /**
     * 카테고리별 매출 비율 조회
     *
     * @return ResponseEntity 메뉴 카테고리별 매출 금액 리스트
     */
    @Operation(
            summary = "카테고리별 매출 비율",
            description = "Redis `sales:category:DATE` (Hash) 의 HGETALL 결과 (카테고리명 별 매출액)."
    )
    @GetMapping("/category/ratio")
    public ResponseEntity<BaseResponse> getCategoryRatio() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getCategoryRatio()));
    }

    /**
     * 결제 수단별 매출 비율 조회
     *
     * @return ResponseEntity 결제 수단별 매출 금액 리스트
     */
    @Operation(
            summary = "결제 수단별 매출 비율",
            description = "Redis `sales:payment:DATE` (Hash) 의 HGETALL 결과 (CARD/CASH 별 매출액)."
    )
    @GetMapping("/payment/ratio")
    public ResponseEntity<BaseResponse> getPaymentMethodRatio() {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getPaymentMethodRatio()));
    }
}
