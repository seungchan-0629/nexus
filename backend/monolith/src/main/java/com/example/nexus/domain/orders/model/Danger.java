package com.example.nexus.domain.orders.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "danger")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Danger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "danger_idx")
    private Long idx;

    @Column(name = "ratio", nullable = false)
    private Integer ratio;

    @Column(name = "period", nullable = false)
    private Integer period;

    public void update(Integer ratio, Integer period) {
        this.ratio = ratio;
        this.period = period;
    }
}
