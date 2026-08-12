package com.example.batch.domain.head.model;

import com.example.batch.common.enums.InventoryStatus;
import com.example.batch.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

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

    @Setter
    @Column(name = "count", nullable = false)
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
