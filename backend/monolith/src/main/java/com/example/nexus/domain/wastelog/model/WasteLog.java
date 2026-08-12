package com.example.nexus.domain.wastelog.model;

import com.example.nexus.domain.product.model.Product;
import com.example.nexus.domain.store.model.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waste_log_idx")
    private Long idx;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "amount_loss", nullable = false)
    private Integer amountLoss;

    @Column(name = "waste_date", nullable = false)
    private LocalDateTime wasteDate;

    @Column(name = "waste_reason", nullable = false)
    private String wasteReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    private Product product;
}
