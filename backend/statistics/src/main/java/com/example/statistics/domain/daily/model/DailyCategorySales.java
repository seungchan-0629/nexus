package com.example.statistics.domain.daily.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_category_sales",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_category",
                columnNames = {"aggregate_date", "category_name"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCategorySales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "aggregate_date", nullable = false)
    private LocalDate aggregateDate;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
