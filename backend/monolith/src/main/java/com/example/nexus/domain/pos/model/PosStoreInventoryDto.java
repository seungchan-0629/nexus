package com.example.nexus.domain.pos.model;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.domain.store.model.StoreInventory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class PosStoreInventoryDto {
    @Getter
    @Builder
    public static class ListRes {
        private Long idx;
        private Integer count;
        private InventoryStatus status;
        private LocalDateTime manufacturedDate;
        private Long storeIdx;
        private String storeName;
        private Long productIdx;
        private String productName;
        private Integer minStock;

        public static PosStoreInventoryDto.ListRes from(PosStoreInventory entity) {
            return PosStoreInventoryDto.ListRes.builder()
                    .idx(entity.getIdx())
                    .count(entity.getCount())
                    .status(entity.getStatus())
                    .manufacturedDate(entity.getManufacturedDate())
                    .storeIdx(entity.getStore().getIdx())
                    .storeName(entity.getStore().getStoreName())
                    .productIdx(entity.getProduct().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .minStock(entity.getProduct().getMinStock())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CountRes {
        private Long idx;
        private Integer count;

        public static PosStoreInventoryDto.CountRes from(PosStoreInventory entity) {
            return PosStoreInventoryDto.CountRes.builder()
                    .idx(entity.getIdx())
                    .count(entity.getCount())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CountReq {
        private Integer count;
    }

    @Getter
    @Builder
    public static class SyncCountRes {
        private Long posInventoryIdx;
        private Integer posCount;
        private Long hqInventoryIdx;
        private Integer hqCount;

        public static PosStoreInventoryDto.SyncCountRes from(PosStoreInventory posEntity, StoreInventory hqEntity) {
            return PosStoreInventoryDto.SyncCountRes.builder()
                    .posInventoryIdx(posEntity.getIdx())
                    .posCount(posEntity.getCount())
                    .hqInventoryIdx(hqEntity.getIdx())
                    .hqCount(hqEntity.getCount())
                    .build();
        }
    }


    @Getter
    @Builder
    public static class OverDueDateProductRes {
        private Long posStoreInventoryIdx;
        private Integer posCount;
        private LocalDateTime manufacturedDate;
        private Long productIdx;
        private Long storeIdx;

        public static PosStoreInventoryDto.OverDueDateProductRes from (PosStoreInventory entity) {
            return OverDueDateProductRes.builder()
                    .posStoreInventoryIdx(entity.getIdx())
                    .posCount(entity.getCount())
                    .manufacturedDate(entity.getManufacturedDate())
                    .productIdx(entity.getProduct().getIdx())
                    .storeIdx(entity.getStore().getIdx())
                    .build();
        }
    }
}
