package com.example.nexus.domain.news.model;

import com.example.nexus.common.enums.NewsCollectCategory;
import com.example.nexus.common.enums.NewsCollectTarget;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class NewsDto {

    @Getter
    @Builder
    public static class SummaryListItem {
        private Long idx;
        private LocalDateTime summaryDate;
        private NewsCollectTarget target;
        private Long storeIdx;
        private String summaryTitle;
        private String summaryContentsPreview;
        private NewsCollectCategory category;
        private String url;
    }

    @Getter
    @Builder
    public static class SummaryDetail {
        private Long idx;
        private LocalDateTime summaryDate;
        private NewsCollectTarget target;
        private Long storeIdx;
        private String summaryTitle;
        private String summaryContents;
        private NewsCollectCategory category;
        private String url;
    }

    @Getter
    @Builder
    public static class CollectRunResult {
        private int hqInserted;
        private int storeInserted;
        private int summariesInserted;
        private String message;
    }
}
