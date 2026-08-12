package com.example.nexus.domain.orders.model;

import lombok.Builder;
import lombok.Getter;

public class OrdersItemDto {

    @Getter
    @Builder
    public static class OrdersItemReq {
        private Integer count;
        private Long productIdx;
    }

    @Getter
    @Builder
    public static class OrdersItemRes {
        private Long idx;
        private Integer count;
        private Long orderIdx;
        private String productName;
        private Integer unitPrice;
        private Integer currentStock;

        public static OrdersItemRes from(OrdersItem entity) {
            return OrdersItemRes.builder()
                    .idx(entity.getIdx())
                    .count(entity.getCount())
                    .orderIdx(entity.getOrders().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .unitPrice(entity.getProduct().getUnitPrice())
                    .build();
        }

        public static OrdersItemRes from(OrdersItem entity, Integer currentStock) {
            return OrdersItemRes.builder()
                    .idx(entity.getIdx())
                    .count(entity.getCount())
                    .orderIdx(entity.getOrders().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .unitPrice(entity.getProduct().getUnitPrice())
                    .currentStock(currentStock)
                    .build();
        }
    }

}
