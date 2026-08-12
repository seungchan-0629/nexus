package com.example.nexus.domain.delivery.model;

import com.example.nexus.common.enums.DeliveryStatus;
import com.example.nexus.domain.orders.model.Orders;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_idx")
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "delay_reason")
    private String delayReason;

    @Column(name = "dep_date", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "est_des_date", nullable = false)
    private LocalDateTime estimatedArrivalAt;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_idx", nullable = false)
    private Orders orders;
}