package com.example.nexus.domain.orders.model;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrdersDto {

    @Getter
    @NoArgsConstructor
    public static class OrdersReq {
        private List<OrdersItemDto.OrdersItemReq> ordersItemList;
    }

    @Getter
    @Builder
    public static class OrdersRes {
        private Long idx;
        private Long price;
        private OrdersType ordersType;
        private OrdersStatus ordersStatus;
        private boolean isDanger;
        private String reason;
        private LocalDateTime createdAt;
        private String storeName;
        private List<OrdersItemDto.OrdersItemRes> ordersItemList;
        private Long deliveryIdx;

        public static OrdersRes from(Orders entity) {
            return OrdersRes.builder()
                    .idx(entity.getIdx())
                    .price(entity.getPrice())
                    .ordersType(entity.getOrdersType())
                    .ordersStatus(entity.getOrdersStatus())
                    .isDanger(entity.isDanger())
                    .reason(entity.getReason())
                    .createdAt(entity.getCreatedAt())
                    .storeName(entity.getStore().getStoreName())
                    .ordersItemList(entity.getOrdersItemList() != null
                            ? entity.getOrdersItemList().stream().map(OrdersItemDto.OrdersItemRes::from).toList()
                            : Collections.emptyList())
                    .deliveryIdx(entity.getDelivery() != null ? entity.getDelivery().getIdx() : null)
                    .build();
        }

        public static OrdersRes fromWithStock(Orders entity, Map<Long, Integer> stockMap) {
            return OrdersRes.builder()
                    .idx(entity.getIdx())
                    .price(entity.getPrice())
                    .ordersType(entity.getOrdersType())
                    .ordersStatus(entity.getOrdersStatus())
                    .isDanger(entity.isDanger())
                    .reason(entity.getReason())
                    .createdAt(entity.getCreatedAt())
                    .storeName(entity.getStore().getStoreName())
                    .ordersItemList(entity.getOrdersItemList() != null ? entity.getOrdersItemList().stream()
                                    .map(item -> OrdersItemDto.OrdersItemRes.from(item, stockMap.get(item.getProduct().getIdx())))
                                    .toList()
                            : Collections.emptyList())
                    .deliveryIdx(entity.getDelivery() != null ? entity.getDelivery().getIdx() : null)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class OrderListRes {
        private Long idx;
        private Long price;
        private OrdersType ordersType;
        private OrdersStatus ordersStatus;
        private LocalDateTime createdAt;
        private String storeName;

        public static OrderListRes from(Orders entity) {
            return OrderListRes.builder()
                    .idx(entity.getIdx())
                    .price(entity.getPrice())
                    .ordersType(entity.getOrdersType())
                    .ordersStatus(entity.getOrdersStatus())
                    .createdAt(entity.getCreatedAt())
                    .storeName(entity.getStore().getStoreName())
                    .build();
        }
    }
}
