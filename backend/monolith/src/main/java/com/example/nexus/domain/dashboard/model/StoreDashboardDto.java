package com.example.nexus.domain.dashboard.model;

import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.orders.model.Orders;
import com.example.nexus.domain.orders.model.OrdersItem;
import com.example.nexus.domain.store.model.StoreInventory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class StoreDashboardDto {

    @Getter
    @Builder
    public static class SalesKpiRes {
        private long todaySales;
        private double deltaPercent;
    }

    @Getter
    @Builder
    public static class PendingOrderKpiRes {
        private long pendingCount;
    }

    @Getter
    @Builder
    public static class InventoryRiskKpiRes {
        private long lowCount;
        private long criticalCount;
        private long totalDangerCount;
    }

    @Getter
    @Builder
    public static class SettlementKpiRes {
        private long currentAmount;
        private String currentPeriod;
        private double deltaPercent;
    }

    @Getter
    @Builder
    public static class DailySalesChartRes {
        private List<String> labels;
        private List<Long> thisWeek;
        private List<Long> lastWeek;
    }

    @Getter
    @Builder
    public static class PendingOrderItem {
        private Long ordersIdx;
        private String productName;
        private int quantity;
        private long price;
        private String status;

        public static PendingOrderItem from(Orders orders) {
            // 첫 번째 품목명 + 외 N건 형태
            List<OrdersItem> items = orders.getOrdersItemList();
            String productName = items.isEmpty() ? "-" : items.get(0).getProduct().getProductName();
            if (items.size() > 1) {
                productName += " 외 " + (items.size() - 1) + "건";
            }
            int totalQty = items.stream().mapToInt(OrdersItem::getCount).sum();

            return PendingOrderItem.builder()
                    .ordersIdx(orders.getIdx())
                    .productName(productName)
                    .quantity(totalQty)
                    .price(orders.getPrice())
                    .status(orders.getOrdersStatus().name())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class MyDeliveryItem {
        private Long deliveryIdx;
        private Long ordersIdx;
        private String status;
        private String estimatedArrival;

        public static MyDeliveryItem from(Delivery entity) {
            return MyDeliveryItem.builder()
                    .deliveryIdx(entity.getIdx())
                    .ordersIdx(entity.getOrders().getIdx())
                    .status(entity.getDeliveryStatus().name())
                    .estimatedArrival(entity.getEstimatedArrivalAt() != null ? entity.getEstimatedArrivalAt().toLocalDate().toString() : null)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class InventoryWarningItem {
        private Long inventoryIdx;
        private String productName;
        private int currentStock;
        private int minStock;
        private String unit;
        private String status;

        public static InventoryWarningItem from(StoreInventory entity) {
            return InventoryWarningItem.builder()
                    .inventoryIdx(entity.getIdx())
                    .productName(entity.getProduct().getProductName())
                    .currentStock(entity.getCount())
                    .minStock(entity.getProduct().getMinStock())
                    .unit(entity.getProduct().getProductUnit())
                    .status(entity.getStatus().name())
                    .build();
        }
    }
}
