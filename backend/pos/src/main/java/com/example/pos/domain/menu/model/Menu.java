package com.example.pos.domain.menu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu implements Persistable<Long> {
    @Id
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

    @Column(name = "menu_category_name",  nullable = false)
    private String menuCategoryName;

    public void update(String menuName, Integer price, String imgPath, String menuCategoryName, boolean isDeleted) {
        this.menuName = menuName;
        this.price = price;
        this.imgPath = imgPath;
        this.menuCategoryName = menuCategoryName;
        this.isDeleted = isDeleted;
    }

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
