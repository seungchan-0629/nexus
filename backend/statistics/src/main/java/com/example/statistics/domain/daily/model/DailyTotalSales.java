package com.example.statistics.domain.daily.model;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_total_sales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTotalSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "aggregate_date", nullable = false, unique = true)
    private LocalDate aggregateDate;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}