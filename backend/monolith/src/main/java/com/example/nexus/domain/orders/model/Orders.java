package com.example.nexus.domain.orders.model;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_idx")
    private Long idx;

    @Column(name = "price", nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrdersType ordersType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrdersStatus ordersStatus;

    @Column(name = "is_danger", nullable = false)
    private boolean isDanger;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx", nullable = false)
    private Store store;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "orders", fetch = FetchType.LAZY)
    private List<OrdersItem> ordersItemList;

    @OneToOne(mappedBy = "orders", fetch = FetchType.LAZY)
    private Delivery delivery;

    public void confirm() {
        this.ordersStatus = OrdersStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    public void cancel() {
        this.ordersStatus = OrdersStatus.CANCELLED;
    }

    public void approve() {
        this.ordersStatus = OrdersStatus.APPROVE;
    }

    public void reject() {
        this.ordersStatus = OrdersStatus.REJECT;
    }

    public void markDanger(boolean isDanger) {
        this.isDanger = isDanger;
    }

    public void updatePrice(Long price) {
        this.price = price;
    }
}
