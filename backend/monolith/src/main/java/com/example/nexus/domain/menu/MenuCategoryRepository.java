package com.example.nexus.domain.menu;

import com.example.nexus.domain.menu.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findAllByIsDeletedFalse();
    Optional<MenuCategory> findByMenuCategoryName(String menuCategoryName);
}
