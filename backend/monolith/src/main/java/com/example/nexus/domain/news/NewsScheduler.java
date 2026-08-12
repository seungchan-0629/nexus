package com.example.nexus.domain.news;

import com.example.nexus.domain.news.model.NewsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsScheduler {

    private final NewsService newsService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void runDailyNewsJob() {
        try {
            NewsDto.CollectRunResult result = newsService.runDailyPipeline();
            log.info("Daily news pipeline: {}", result.getMessage());
        } catch (Exception e) {
            log.error("Daily news pipeline failed", e);
        }
    }
}
