package com.example.pos.domain.menu;

import com.example.pos.domain.menu.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByMenu_Idx(Long menuIdx);
}
