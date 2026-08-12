package com.example.nexus.domain.news.model;

import com.example.nexus.common.enums.NewsCollectCategory;
import com.example.nexus.common.enums.NewsCollectTarget;
import com.example.nexus.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_summary")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_summary_idx")
    private Long idx;

    @Column(name = "summary_date", nullable = false)
    private LocalDateTime summaryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false)
    private NewsCollectTarget target;

    @Column(name = "summary_title", nullable = false)
    private String summaryTitle;

    @Column(name = "summary_contents", nullable = false, columnDefinition = "TEXT")
    private String summaryContents;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private NewsCollectCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx")
    private Store store;


}
