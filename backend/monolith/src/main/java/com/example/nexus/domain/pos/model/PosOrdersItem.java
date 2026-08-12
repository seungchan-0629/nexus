package com.example.nexus.domain.pos.model;

import com.example.nexus.domain.menu.model.Menu;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pos_orders_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosOrdersItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pos_orders_item_idx", nullable = false)
    private Long idx;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_idx", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_pay_idx", nullable = false)
    private PosPay posPay;
}
