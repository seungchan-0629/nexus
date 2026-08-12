package com.example.batch.domain.head.model;

import com.example.batch.domain.orders.model.Orders;
import com.example.batch.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "head_income")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeadIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "head_income_idx")
    private Long idx;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "settlement_idx")
    private Long settlementIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx")
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_idx", nullable = false)
    private Orders orders;
}
