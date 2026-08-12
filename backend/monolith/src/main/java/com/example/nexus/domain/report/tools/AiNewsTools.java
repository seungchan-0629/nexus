package com.example.nexus.domain.report.tools;

import com.example.nexus.common.enums.NewsCollectTarget;
import com.example.nexus.domain.news.NewsRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

// 뉴스(News) 데이터 조회 도구 — AI가 대화 맥락에 맞춰 호출
@Component
public class AiNewsTools {

    private final NewsRepository newsRepository;

    public AiNewsTools(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // 오늘 수집된 본사용 뉴스 요약 목록
    @Tool(description = "오늘 수집된 본사용 뉴스 요약 목록을 조회한다. 각 항목은 제목, 요약, 카테고리.")
    public List<Map<String, Object>> getTodayNews() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return newsRepository.findByTargetAndStoreIsNullAndSummaryDateBetweenOrderBySummaryDateDesc(
                        NewsCollectTarget.HQ, start, end).stream()
                .map(n -> Map.<String, Object>of(
                        "title", n.getSummaryTitle(),
                        "summary", n.getSummaryContents(),
                        "category", String.valueOf(n.getCategory())))
                .toList();
    }
}
