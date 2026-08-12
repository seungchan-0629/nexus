package com.example.batch.domain.store.model;

import com.example.batch.common.enums.InventoryStatus;
import com.example.batch.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_inventory_idx")
    private Long idx;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20)")
    private InventoryStatus status = InventoryStatus.NORMAL;

    @Column(name = "manufactured_date", nullable = false)
    private LocalDateTime manufacturedDate;

    @Column(name = "avg_stock", nullable = false)
    private Integer avgStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    private Product product;

    // 롤백 식별용: 어떤 orders_item으로 생성된 재고인지 추적
    @Column(name = "orders_item_idx")
    private Long ordersItemIdx;
}
