package com.example.nexus.domain.orders.model;

import com.example.nexus.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders_item")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrdersItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_item_idx")
    private Long idx;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Builder.Default
    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_idx", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    private Product product;

    public void updateCount(Integer count) {
        this.count = count;
    }
}
