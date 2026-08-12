package com.example.batch.domain.orders.model;

import com.example.batch.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders_item")
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

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_idx", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    private Product product;

    public void resetProcessed() {
        this.processed = false;
    }
}
