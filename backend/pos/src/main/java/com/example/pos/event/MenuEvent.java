package com.example.pos.event;

import java.util.List;

public record MenuEvent(
        Long menuIdx,
        String menuName,
        Integer price,
        String menuCategoryName,
        String imgPath,
        Boolean isDeleted,
        List<MenuItemEvent> menuItems
) {
    public record MenuItemEvent(
            Long idx,
            Integer quantity,
            String menuUnit,
            Long productIdx
    ){}
}
