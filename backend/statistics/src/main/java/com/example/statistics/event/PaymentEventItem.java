package com.example.statistics.event;

public record PaymentEventItem(
        Long menuIdx,
        String menuName,
        Long menuCategoryIdx,
        String menuCategoryName,
        Integer unitPrice,
        Integer quantity,
        Long lineAmount           // unitPrice * quantity
) {}