package com.example.pos.model;

import com.example.pos.common.enums.PosPayMethod;
import com.example.pos.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pos_pay")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosPay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pos_pay_idx")
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PosPayMethod method;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "pay_amount", nullable = false)
    private Long payAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx", nullable = false)
    private Store store;

    @Column(name = "store_inventory_deducted_at")
    private LocalDateTime storeInventoryDeductedAt;

}
