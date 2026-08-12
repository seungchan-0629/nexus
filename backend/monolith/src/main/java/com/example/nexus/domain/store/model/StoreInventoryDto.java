package com.example.nexus.domain.store.model;

import com.example.nexus.common.enums.InventoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class StoreInventoryDto {
    @Getter
    @Builder
    public static class ListRes {
        private Long idx;
        private Integer count;
        private Integer avgStock;
        private InventoryStatus status;
        private LocalDateTime manufacturedDate;
        private LocalDateTime expiryDate;
        private Long storeIdx;
        private String storeName;
        private Long productIdx;
        private String productName;
        private Integer minStock;

        public static ListRes from(StoreInventory entity) {
            int dangerDays = parseDangerDays(entity.getProduct().getDangerDays());
            return ListRes.builder()
                    .idx(entity.getIdx())
                    .count(entity.getCount())
                    .avgStock(entity.getAvgStock())
                    .status(entity.getStatus())
                    .manufacturedDate(entity.getManufacturedDate())
                    .expiryDate(entity.getManufacturedDate().plusDays(dangerDays))
                    .storeIdx(entity.getStore().getIdx())
                    .storeName(entity.getStore().getStoreName())
                    .productIdx(entity.getProduct().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .minStock(entity.getProduct().getMinStock())
                    .build();
        }

        private static int parseDangerDays(String dangerDays) {
            try {
                return Integer.parseInt(dangerDays);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
