package com.example.nexus.domain.statistics.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class StatisticsDto {

    @Getter
    @Builder
    public static class TodaySalesRes {
        private long todaySales;
    }

    @Getter
    @Builder
    public static class RankingItem {
        private Long idx;
        private String name;
        private long score;
    }

    @Getter
    @Builder
    public static class RankingRes {
        private List<RankingItem> rankings;
    }

    @Getter
    @Builder
    public static class HourlySalesItem {
        private int hour;
        private long sales;
    }

    @Getter
    @Builder
    public static class HourlySalesRes {
        private List<HourlySalesItem> hourlyData;
    }

    @Getter
    @Builder
    public static class RatioItem {
        private String name;
        private long amount;
    }

    @Getter
    @Builder
    public static class RatioRes {
        private List<RatioItem> ratios;
    }
}
