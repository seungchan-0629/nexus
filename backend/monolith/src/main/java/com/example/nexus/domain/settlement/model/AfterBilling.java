package com.example.nexus.domain.settlement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "after_billing")
public class AfterBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private Long storeIdx;

    private Integer totalPayAmount;

    private String payedMonth; // yyyy-MM 형식

    private Boolean isPaid;

    private Boolean isSuccess;

    private String failReason;

    @Builder.Default
    private Boolean isRetryFailed = false;

    private LocalDateTime createdAt;
}
