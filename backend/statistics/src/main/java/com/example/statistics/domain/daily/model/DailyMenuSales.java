package com.example.statistics.domain.daily.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_menu_sales",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_menu",
                columnNames = {"aggregate_date", "menu_idx"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMenuSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "aggregate_date", nullable = false)
    private LocalDate aggregateDate;

    @Column(name = "menu_idx", nullable = false)
    private Long menuIdx;

    @Column(name = "menu_name", nullable = false, length = 200)
    private String menuName;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
