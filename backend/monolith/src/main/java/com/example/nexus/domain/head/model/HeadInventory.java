package com.example.nexus.domain.head.model;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.domain.product.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "head_inventory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeadInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "head_inventory_idx")
    private Long idx;

    @Column(name = "count", nullable = false)
    @Setter
    private Integer count;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20)")
    private InventoryStatus status = InventoryStatus.NORMAL;

    @Column(name = "manufactured_date", nullable = false)
    private LocalDateTime manufacturedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    private Product product;
}
