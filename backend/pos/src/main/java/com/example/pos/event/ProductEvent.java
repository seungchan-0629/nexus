package com.example.pos.event;

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
