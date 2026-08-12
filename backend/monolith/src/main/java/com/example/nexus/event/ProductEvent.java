package com.example.nexus.event;

public record ProductEvent(
        Long productIdx,
        String productName,
        String productUnit,
        Integer maxStock,
        Integer minStock,
        Integer unitPrice,
        String dangerDays,
        boolean isDeleted,
        String categoryName
) {
}