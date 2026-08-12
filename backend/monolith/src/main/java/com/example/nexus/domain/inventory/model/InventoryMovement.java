package com.example.nexus.domain.inventory.model;

import com.example.nexus.common.enums.MovementLocationType;
import com.example.nexus.common.enums.MovementType;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_movement_idx")
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_location_type", nullable = false)
    private MovementLocationType fromLocationType;

    @Column(name = "from_ref_idx")
    private Long fromRefIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_location_type", nullable = false)
    private MovementLocationType toLocationType;

    @Column(name = "to_ref_idx")
    private Long toRefIdx;

    @Column(name = "memo")
    private String memo;

    // 롤백 식별용: 어떤 주문으로 생성된 이동 기록인지 추적
    @Column(name = "orders_idx")
    private Long ordersIdx;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static InventoryMovement inbound(Product product, Integer quantity, String memo) {
        return new InventoryMovement(
                null,
                product,
                MovementType.INBOUND,
                quantity,
                MovementLocationType.NONE,
                null,
                MovementLocationType.HEAD,
                null,
                memo,
                null,   // ordersIdx
                null    // createdAt
        );
    }

    public static InventoryMovement transferOut(Product product, Long storeIdx, Integer quantity, String memo) {
        return new InventoryMovement(
                null,
                product,
                MovementType.TRANSFER_OUT,
                quantity,
                MovementLocationType.HEAD,
                null,
                MovementLocationType.STORE,
                storeIdx,
                memo,
                null,   // ordersIdx
                null    // createdAt
        );
    }
}
