package com.example.statistics;

import com.example.statistics.common.model.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장기 통계 (월/연도/매장/카테고리별) REST API.
 * daily_* 테이블의 영구 보존 데이터를 기반으로 조회.
 *
 * URL prefix: /statistics/long-term/*
 */
@Tag(name = "장기 통계", description = "daily_*_sales 테이블 기반 영구 보존 통계 (#881, #889)")
@RestController
@RequestMapping("/statistics/long-term")
@RequiredArgsConstructor
public class LongTermStatisticsController {

    private final LongTermStatisticsService longTermStatisticsService;

    /**
     * 연도별 총 매출 (전체 기간).
     *
     * GET /statistics/long-term/yearly
     */
    @Operation(
            summary = "연도별 총 매출",
            description = "전체 기간 연도별 매출 합계 (daily_total_sales 의 GROUP BY year)."
    )
    @GetMapping("/yearly")
    public ResponseEntity<BaseResponse> getYearly() {
        return ResponseEntity.ok(BaseResponse.success(longTermStatisticsService.getYearlySales()));
    }

    /**
     * 특정 연도의 월별 매출 (1~12월).
     *
     * GET /statistics/long-term/monthly?year=2026
     *
     * @param year 조회할 연도
     */
    @Operation(
            summary = "월별 매출",
            description = "특정 연도의 1~12월 매출 합계."
    )
    @GetMapping("/monthly")
    public ResponseEntity<BaseResponse> getMonthly(
            @Parameter(
                    description = "조회할 연도",
                    examples = {
                            @ExampleObject(name = "현재 연도", value = "2026"),
                            @ExampleObject(name = "작년", value = "2025"),
                            @ExampleObject(name = "데이터 없는 연도 (빈 결과)", value = "2099")
                    }
            )
            @RequestParam int year
    ) {
        return ResponseEntity.ok(BaseResponse.success(longTermStatisticsService.getMonthlySales(year)));
    }

    /**
     * 특정 연도의 분기별 매출 (1~4분기).
     *
     * GET /statistics/long-term/quarterly?year=2026
     *
     * @param year 조회할 연도
     */
    @Operation(
            summary = "분기별 매출",
            description = "특정 연도의 1~4분기 매출 합계."
    )
    @GetMapping("/quarterly")
    public ResponseEntity<BaseResponse> getQuarterly(
            @Parameter(
                    description = "조회할 연도",
                    examples = {
                            @ExampleObject(name = "현재 연도", value = "2026"),
                            @ExampleObject(name = "작년", value = "2025"),
                            @ExampleObject(name = "데이터 없는 연도 (빈 결과)", value = "2099")
                    }
            )
            @RequestParam int year
    ) {
        return ResponseEntity.ok(BaseResponse.success(longTermStatisticsService.getQuarterlySales(year)));
    }

    /**
     * 특정 기간 매장별 매출 (매출 큰 순).
     *
     * GET /statistics/long-term/stores?year=2026&month=5   (월 단위)
     * GET /statistics/long-term/stores?year=2026           (연 단위)
     *
     * @param year  조회할 연도
     * @param month 조회할 월 (선택, 없으면 그 해 전체)
     */
    @Operation(
            summary = "매장별 매출",
            description = "특정 기간 매장별 매출 합계 (매출 큰 순). month 가 null 이면 그 해 전체."
    )
    @GetMapping("/stores")
    public ResponseEntity<BaseResponse> getStores(
            @Parameter(
                    description = "조회할 연도",
                    examples = {
                            @ExampleObject(name = "현재 연도", value = "2026"),
                            @ExampleObject(name = "작년", value = "2025"),
                            @ExampleObject(name = "데이터 없는 연도 (빈 결과)", value = "2099")
                    }
            )
            @RequestParam int year,
            @Parameter(
                    description = "조회할 월 (1~12, 선택). 없으면 연 단위 합계",
                    examples = {
                            @ExampleObject(name = "5월", value = "5"),
                            @ExampleObject(name = "12월 (연말)", value = "12"),
                            @ExampleObject(name = "1월 (연초)", value = "1")
                    }
            )
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                longTermStatisticsService.getStoreSales(year, month)
        ));
    }

    /**
     * 특정 기간 카테고리별 매출 (매출 큰 순).
     *
     * GET /statistics/long-term/categories?year=2026&month=5
     * GET /statistics/long-term/categories?year=2026
     *
     * @param year  조회할 연도
     * @param month 조회할 월 (선택, 없으면 그 해 전체)
     */
    @Operation(
            summary = "카테고리별 매출",
            description = "특정 기간 메뉴 카테고리별 매출 합계 (매출 큰 순)."
    )
    @GetMapping("/categories")
    public ResponseEntity<BaseResponse> getCategories(
            @Parameter(
                    description = "조회할 연도",
                    examples = {
                            @ExampleObject(name = "현재 연도", value = "2026"),
                            @ExampleObject(name = "작년", value = "2025"),
                            @ExampleObject(name = "데이터 없는 연도 (빈 결과)", value = "2099")
                    }
            )
            @RequestParam int year,
            @Parameter(
                    description = "조회할 월 (1~12, 선택). 없으면 연 단위 합계",
                    examples = {
                            @ExampleObject(name = "5월", value = "5"),
                            @ExampleObject(name = "12월 (연말)", value = "12"),
                            @ExampleObject(name = "1월 (연초)", value = "1")
                    }
            )
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                longTermStatisticsService.getCategorySales(year, month)
        ));
    }

    /**
     * 특정 기간 메뉴별 판매량 (판매량 많은 순).
     *
     * GET /statistics/long-term/menus?year=2026&month=5
     * GET /statistics/long-term/menus?year=2026
     *
     * @param year  조회할 연도
     * @param month 조회할 월 (선택, 없으면 그 해 전체)
     */
    @Operation(
            summary = "메뉴별 판매량",
            description = "특정 기간 메뉴별 판매 수량 합계 (판매량 많은 순)."
    )
    @GetMapping("/menus")
    public ResponseEntity<BaseResponse> getMenus(
            @Parameter(
                    description = "조회할 연도",
                    examples = {
                            @ExampleObject(name = "현재 연도", value = "2026"),
                            @ExampleObject(name = "작년", value = "2025"),
                            @ExampleObject(name = "데이터 없는 연도 (빈 결과)", value = "2099")
                    }
            )
            @RequestParam int year,
            @Parameter(
                    description = "조회할 월 (1~12, 선택). 없으면 연 단위 합계",
                    examples = {
                            @ExampleObject(name = "5월", value = "5"),
                            @ExampleObject(name = "12월 (연말)", value = "12"),
                            @ExampleObject(name = "1월 (연초)", value = "1")
                    }
            )
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                longTermStatisticsService.getMenuSales(year, month)
        ));
    }
}
