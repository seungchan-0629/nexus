package com.example.statistics;

import com.example.statistics.domain.daily.DailyCategorySalesRepository;
import com.example.statistics.domain.daily.DailyMenuSalesRepository;
import com.example.statistics.domain.daily.DailyStoreSalesRepository;
import com.example.statistics.domain.daily.DailyTotalSalesRepository;
import com.example.statistics.model.LongTermStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 장기 통계 (월별/연도별/매장별/카테고리별) 비즈니스 로직.
 * daily_* 테이블의 GROUP BY 결과를 DTO 로 변환해 반환.
 */
@Service
@RequiredArgsConstructor
public class LongTermStatisticsService {

    private final DailyTotalSalesRepository totalRepo;
    private final DailyStoreSalesRepository storeRepo;
    private final DailyCategorySalesRepository categoryRepo;
    private final DailyMenuSalesRepository menuRepo;

    /**
     * 연도별 총 매출 (전체 기간).
     */
    public LongTermStatisticsDto.YearlySalesRes getYearlySales() {
        List<LongTermStatisticsDto.YearlySalesItem> items = totalRepo.findYearlySalesGroup().stream()
                .map(r -> LongTermStatisticsDto.YearlySalesItem.builder()
                        .year(((Number) r[0]).intValue())
                        .total(r[1] == null ? 0L : ((Number) r[1]).longValue())
                        .build())
                .toList();
        return LongTermStatisticsDto.YearlySalesRes.builder()
                .yearlyData(items)
                .build();
    }

    /**
     * 특정 연도의 월별 매출 (1~12월).
     *
     * @param year 조회할 연도
     */
    public LongTermStatisticsDto.MonthlySalesRes getMonthlySales(int year) {
        List<LongTermStatisticsDto.MonthlySalesItem> items = totalRepo.findMonthlySalesGroup(year).stream()
                .map(r -> LongTermStatisticsDto.MonthlySalesItem.builder()
                        .month(((Number) r[0]).intValue())
                        .total(r[1] == null ? 0L : ((Number) r[1]).longValue())
                        .build())
                .toList();
        return LongTermStatisticsDto.MonthlySalesRes.builder()
                .year(year)
                .monthlyData(items)
                .build();
    }

    /**
     * 특정 연도의 분기별 매출 (1~4분기).
     *
     * @param year 조회할 연도
     */
    public LongTermStatisticsDto.QuarterlySalesRes getQuarterlySales(int year) {
        List<LongTermStatisticsDto.QuarterlySalesItem> items = totalRepo.findQuarterlySalesGroup(year).stream()
                .map(r -> LongTermStatisticsDto.QuarterlySalesItem.builder()
                        .quarter(((Number) r[0]).intValue())
                        .total(r[1] == null ? 0L : ((Number) r[1]).longValue())
                        .build())
                .toList();
        return LongTermStatisticsDto.QuarterlySalesRes.builder()
                .year(year)
                .quarterlyData(items)
                .build();
    }

    /**
     * 특정 기간의 매장별 매출 합계 (매출 큰 순).
     * month 가 null 이면 해당 연도 전체.
     *
     * @param year  조회할 연도
     * @param month 조회할 월 (선택)
     */
    public LongTermStatisticsDto.StoreSalesRes getStoreSales(int year, Integer month) {
        LocalDate[] range = calculateDateRange(year, month);

        List<LongTermStatisticsDto.StoreSalesItem> items = storeRepo
                .findStoreSalesGroupByRange(range[0], range[1]).stream()
                .map(r -> LongTermStatisticsDto.StoreSalesItem.builder()
                        .storeIdx(((Number) r[0]).longValue())
                        .storeName((String) r[1])
                        .total(r[2] == null ? 0L : ((Number) r[2]).longValue())
                        .build())
                .toList();
        return LongTermStatisticsDto.StoreSalesRes.builder()
                .stores(items)
                .build();
    }

    /**
     * 특정 기간의 카테고리별 매출 합계 (매출 큰 순).
     * month 가 null 이면 해당 연도 전체.
     *
     * @param year  조회할 연도
     * @param month 조회할 월 (선택)
     */
    public LongTermStatisticsDto.CategorySalesRes getCategorySales(int year, Integer month) {
        LocalDate[] range = calculateDateRange(year, month);

        List<LongTermStatisticsDto.CategorySalesItem> items = categoryRepo
                .findCategorySalesGroupByRange(range[0], range[1]).stream()
                .map(r -> LongTermStatisticsDto.CategorySalesItem.builder()
                        .categoryName((String) r[0])
                        .amount(r[1] == null ? 0L : ((Number) r[1]).longValue())
                        .build())
                .toList();
        return LongTermStatisticsDto.CategorySalesRes.builder()
                .categories(items)
                .build();
    }

    /**
     * 특정 기간의 메뉴별 판매량 합계 (판매량 많은 순).
     *
     * @param year  조회할 연도
     * @param month 조회할 월 (선택)
     */
    public LongTermStatisticsDto.MenuSalesRes getMenuSales(int year, Integer month) {
        LocalDate[] range = calculateDateRange(year, month);

        List<LongTermStatisticsDto.MenuSalesItem> items = menuRepo
                .findMenuSalesGroupByRange(range[0], range[1]).stream()
                .map(r -> LongTermStatisticsDto.MenuSalesItem.builder()
                        .menuIdx(((Number) r[0]).longValue())
                        .menuName((String) r[1])
                        .totalQuantity(r[2] == null ? 0L : ((Number) r[2]).longValue())
                        .build())
                .toList();
        return LongTermStatisticsDto.MenuSalesRes.builder()
                .menus(items)
                .build();
    }

    /**
     * year + month(선택) 로부터 시작/종료 날짜 계산.
     * month null → 그 해 1/1 ~ 12/31
     * month 있음 → 그 달 1일 ~ 말일
     */
    private LocalDate[] calculateDateRange(int year, Integer month) {
        LocalDate start, end;
        if (month != null) {
            start = LocalDate.of(year, month, 1);
            end = start.plusMonths(1).minusDays(1);
        } else {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        }
        return new LocalDate[]{start, end};
    }
}
