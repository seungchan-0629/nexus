package com.example.statistics.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 장기 통계 (월별/연도별/매장별/카테고리별) 응답 DTO.
 * daily_* 테이블의 GROUP BY 결과를 담는다.
 */
public class LongTermStatisticsDto {

    // ─── 연도별 ─────────────────────────────────────

    @Getter
    @Builder
    public static class YearlySalesItem {
        private int year;
        private long total;
    }

    @Getter
    @Builder
    public static class YearlySalesRes {
        private List<YearlySalesItem> yearlyData;
    }

    // ─── 월별 ───────────────────────────────────────

    @Getter
    @Builder
    public static class MonthlySalesItem {
        private int month;
        private long total;
    }

    @Getter
    @Builder
    public static class MonthlySalesRes {
        private int year;
        private List<MonthlySalesItem> monthlyData;
    }

    // ─── 분기별 ─────────────────────────────────────

    @Getter
    @Builder
    public static class QuarterlySalesItem {
        private int quarter;   // 1~4
        private long total;
    }

    @Getter
    @Builder
    public static class QuarterlySalesRes {
        private int year;
        private List<QuarterlySalesItem> quarterlyData;
    }

    // ─── 매장별 ─────────────────────────────────────

    @Getter
    @Builder
    public static class StoreSalesItem {
        private Long storeIdx;
        private String storeName;
        private long total;
    }

    @Getter
    @Builder
    public static class StoreSalesRes {
        private List<StoreSalesItem> stores;
    }

    // ─── 카테고리별 ─────────────────────────────────

    @Getter
    @Builder
    public static class CategorySalesItem {
        private String categoryName;
        private long amount;
    }

    @Getter
    @Builder
    public static class CategorySalesRes {
        private List<CategorySalesItem> categories;
    }

    // ─── 메뉴별 ─────────────────────────────────────

    @Getter
    @Builder
    public static class MenuSalesItem {
        private Long menuIdx;
        private String menuName;
        private long totalQuantity;
    }

    @Getter
    @Builder
    public static class MenuSalesRes {
        private List<MenuSalesItem> menus;
    }
}
