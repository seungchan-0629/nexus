package com.example.nexus.domain.dashboard.model;

import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.head.model.HeadInventory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class DashboardDto {

    @Getter
    @Builder
    public static class StoreKpiRes {
        private long totalStoreCount;
        private long newStoreCountThisMonth;
        private double deltaPercent;
    }

    @Getter
    @Builder
    public static class OrdersKpiRes {
        private long todayAutoCount;
        private long confirmedCount;
        private long todayManualCount;
        private long dangerCount;
    }

    @Getter
    @Builder
    public static class DeliveryKpiRes {
        private long ongoingCount;
        private long delayCount;
    }

    @Getter
    @Builder
    public static class DeliveryItem {
        private Long deliveryIdx;
        private Long ordersIdx;
        private String storeName;
        private String status;

        public static DeliveryItem from(Delivery entity) {
            var orders = entity.getOrders();
            return DeliveryItem.builder()
                    .deliveryIdx(entity.getIdx())
                    .ordersIdx(orders != null ? orders.getIdx() : null)
                    .storeName(orders != null && orders.getStore() != null
                            ? orders.getStore().getStoreName() : null)
                    .status(entity.getDeliveryStatus().name())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DangerStatsRes {
        private List<String> labels;
        private List<Long> confirmed;
        private List<Long> approved;
        private List<Long> rejected;
    }

    @Getter
    @Builder
    public static class WeeklyOrderStatsRes {
        private List<String> labels;
        private List<Long> thisWeek;
        private List<Long> lastWeek;
    }

    @Getter
    @Builder
    public static class DeliveryRatioRes {
        private long ready;
        private long start;
        private long delivering;
        private long delivered;
        private long delay;
    }

    @Getter
    @Builder
    public static class InventoryKpiRes {
        private long lowCount;
        private long criticalCount;
        private long totalDangerCount;
    }

    @Getter
    @Builder
    public static class DangerInventoryItem {
        private Long inventoryIdx;
        private String productName;
        private int count;
        private int minStock;
        private String status;

        public static DangerInventoryItem from(HeadInventory entity) {
            return DangerInventoryItem.builder()
                    .inventoryIdx(entity.getIdx())
                    .productName(entity.getProduct().getProductName())
                    .count(entity.getCount())
                    .minStock(entity.getProduct().getMinStock())
                    .status(entity.getStatus().name())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DangerInventoryRes {
        private List<DangerInventoryItem> items;
        private boolean hasNext;
    }

    @Getter
    @Builder
    public static class DelayDeliveryRes {
        private List<DeliveryItem> items;
        private boolean hasNext;
    }

}
