package com.example.statistics.domain.daily.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_store_sales",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_store",
                columnNames = {"aggregate_date", "store_idx"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStoreSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "aggregate_date", nullable = false)
    private LocalDate aggregateDate;

    @Column(name = "store_idx", nullable = false)
    private Long storeIdx;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
