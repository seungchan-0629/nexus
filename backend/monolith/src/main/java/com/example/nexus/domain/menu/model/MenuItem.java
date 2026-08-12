package com.example.nexus.domain.menu.model;

import com.example.nexus.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_idx")
    private Long idx;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "menu_unit", nullable = false)
    private String menuUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_idx", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx", nullable = false)
    @Setter
    private Product product;
}
