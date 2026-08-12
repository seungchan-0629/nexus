package com.example.nexus.domain.menu.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_idx")
    private Long idx;

    @Column(name = "menu_name", nullable = false, unique = true)
    private String menuName;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "img_path", nullable = false)
    private String imgPath;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItemList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_idx")
    private MenuCategory menuCategory;

    public void update(String menuName, Integer price, String imgPath, MenuCategory category) {
        this.menuName = menuName;
        this.price = price;
        this.imgPath = imgPath;
        this.menuCategory = category;
    }

    public void deleteTrue() {
        this.isDeleted = true;
    }
}
