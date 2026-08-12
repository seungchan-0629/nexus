package com.example.nexus.domain.news;

import com.example.nexus.common.enums.NewsCollectTarget;
import com.example.nexus.domain.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByTargetAndStoreIsNullAndSummaryDateBetweenOrderBySummaryDateDesc(
            NewsCollectTarget target, LocalDateTime start, LocalDateTime end);

    List<News> findByTargetAndStore_IdxAndSummaryDateBetweenOrderBySummaryDateDesc(
            NewsCollectTarget target, Long storeIdx, LocalDateTime start, LocalDateTime end);
}
