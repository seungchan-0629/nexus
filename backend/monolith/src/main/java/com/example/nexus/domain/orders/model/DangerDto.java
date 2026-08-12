package com.example.nexus.domain.orders.model;

import com.example.nexus.common.enums.OrdersStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class DangerDto {

    @Getter
    @Builder
    public static class DangerReq {
        private Integer ratio;
        private Integer period;
    }

    @Getter
    @Builder
    public static class DangerRes {
        private Integer ratio;
        private Integer period;

        public static DangerRes from(Danger entity) {
            return DangerRes.builder()
                    .ratio(entity.getRatio())
                    .period(entity.getPeriod())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DangerListRes {
        private Long idx;
        private String storeName;
        private LocalDateTime createdAt;
        private Long price;
        private OrdersStatus ordersStatus;
        private Integer totalQty;
        private Integer avgQty;
        private Integer ratio;

        public static DangerListRes from(Orders entity, int avgQty, int totalQty) {
            int ratioValue = avgQty > 0 ? (totalQty - avgQty) * 100 / avgQty : 0;

            return DangerListRes.builder()
                    .idx(entity.getIdx())
                    .storeName(entity.getStore().getStoreName())
                    .createdAt(entity.getCreatedAt())
                    .price(entity.getPrice())
                    .ordersStatus(entity.getOrdersStatus())
                    .totalQty(totalQty)
                    .avgQty(avgQty)
                    .ratio(ratioValue)
                    .build();
        }
    }
}
