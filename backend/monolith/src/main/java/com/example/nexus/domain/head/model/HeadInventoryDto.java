package com.example.nexus.domain.head.model;

import com.example.nexus.common.enums.InventoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class HeadInventoryDto {
    @Getter
    @Builder
    public static class ListRes {
        private Long idx;
        private Integer count;
        private InventoryStatus status;
        private LocalDateTime manufacturedDate;
        private Long productIdx;
        private String productName;

        public static ListRes from(HeadInventory entity) {
            return ListRes.builder()
                    .idx(entity.getIdx())
                    .count(entity.getCount())
                    .status(entity.getStatus())
                    .manufacturedDate(entity.getManufacturedDate())
                    .productIdx(entity.getProduct().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .build();
        }
    }
}
