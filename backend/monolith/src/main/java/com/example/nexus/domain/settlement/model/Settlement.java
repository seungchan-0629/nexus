package com.example.nexus.domain.settlement.model;

import com.example.nexus.domain.orders.model.Orders;
import com.example.nexus.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settlement")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_idx")
    private Long idx;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "pg_payment_id")
    private String pgPaymentId;

    private Long storeIdx;

    @Column(name = "settlement_ym", nullable = false, length = 7)
    private String settlementYm;
}
