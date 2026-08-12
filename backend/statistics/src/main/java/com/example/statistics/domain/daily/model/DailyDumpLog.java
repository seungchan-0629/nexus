package com.example.statistics.domain.daily.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_dump_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDumpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "aggregate_date", nullable = false, unique = true)
    private LocalDate aggregateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "dump_status", nullable = false, length = 20)
    private DumpStatus dumpStatus;

    @Column(name = "record_count", nullable = false)
    private Integer recordCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "dumped_at", nullable = false, updatable = false)
    private LocalDateTime dumpedAt;
}
