package com.example.pos.domain.menu.model;

import com.example.pos.domain.product.model.Product;
import com.example.pos.domain.menu.model.Menu;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "menu_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem implements Persistable<Long> {
    @Id
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

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public Long getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad @PrePersist
    void markNotNew() { this.isNew = false; }
}
