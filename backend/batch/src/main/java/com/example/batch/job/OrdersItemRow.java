package com.example.batch.job;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrdersItemRow {
    private final Long ordersItemIdx;
    private final Integer count;
    private final Long ordersIdx;
    private final Long storeIdx;
    private final Long productIdx;
    private final Integer minStock;
}
