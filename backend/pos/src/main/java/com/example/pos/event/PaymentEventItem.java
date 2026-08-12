package com.example.pos.event;

public record PaymentEventItem(
        Long menuIdx,
        String menuName,
        Long menuCategoryIdx,
        String menuCategoryName,
        Integer unitPrice,
        Integer quantity,
        Long lineAmount           // unitPrice * quantity
) {}