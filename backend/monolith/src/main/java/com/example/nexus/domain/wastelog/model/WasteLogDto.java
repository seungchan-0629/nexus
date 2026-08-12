package com.example.nexus.domain.wastelog.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class WasteLogDto {

    @Builder
    @Getter
    public static class RegRes {
        private Long idx;
        private int amountLoss;
        private int quantity;
        private LocalDateTime wasteDate;
        private String wasteReason;
        private Long productIdx;
        private Long storeIdx;

        public static WasteLogDto.RegRes from (WasteLog entity) {
            return RegRes.builder()
                    .idx(entity.getIdx())
                    .amountLoss(entity.getAmountLoss())
                    .quantity(entity.getQuantity())
                    .wasteDate(entity.getWasteDate())
                    .wasteReason(entity.getWasteReason())
                    .productIdx(entity.getProduct().getIdx())
                    .storeIdx(entity.getStore().getIdx())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WasteReq {
        private Long storeInventoryIdx;

        @Min(value = 1, message = "폐기 수량은 1 이상이어야 합니다.")
        private Integer quantity;

        @NotBlank(message = "폐기 사유를 입력해주세요.")
        private String wasteReason;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PosWasteReq {
        @NotNull(message = "POS 재고 lot id는 필수입니다.")
        @Schema(example = "1", description = "폐기할 POS 재고 lot ID")
        private Long posStoreInventoryIdx;

        @Min(value = 1, message = "폐기 수량은 1 이상이어야 합니다.")
        @Schema(example = "3", description = "폐기 수량 (1 이상)")
        private Integer quantity;

        @NotBlank(message = "폐기 사유를 입력해주세요.")
        @Schema(example = "유통기한 초과", description = "폐기 사유")
        private String wasteReason;
    }

    @Getter
    @Builder
    public static class WasteRes {
        private Long wasteLogIdx;
        private Long storeIdx;
        private String storeName;
        private Long productIdx;
        private String productName;
        private Integer quantity;
        private Long amountLoss;
        private LocalDateTime wasteDate;
        private String wasteReason;

        public static WasteRes from(WasteLog entity) {
            return WasteRes.builder()
                    .wasteLogIdx(entity.getIdx())
                    .storeIdx(entity.getStore().getIdx())
                    .storeName(entity.getStore().getStoreName())
                    .productIdx(entity.getProduct().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .quantity(entity.getQuantity())
                    .amountLoss(Long.valueOf(entity.getAmountLoss()))
                    .wasteDate(entity.getWasteDate())
                    .wasteReason(entity.getWasteReason())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class StatsRes {
        private Long wasteSum;
        private Long lastMonthWasteSum;
        private Float wasteGradient;
        private Float useSuccessRate;
        private Long wasteSavingAmount;
        private Float avgTurnover;
        private Float optimalStockRate;
        private Integer overstockCount;
        private Float autoOrderAccuracy;
        private List<MonthlyTrend> monthlyWasteTrend;
        private List<CategoryRatio> categoryWasteRatio;
        private List<StoreStatus> storeWasteStatus;
        private List<StoreTurnover> storeTurnoverTrend;
        private List<OverstockTrend> storeOverstockTrend;
    }

    @Builder
    @Getter
    public static class MonthlyTrend {
        private String month;
        private Long quantity;
    }

    @Builder
    @Getter
    public static class CategoryRatio {
        private String categoryName;
        private Long quantity;
    }

    @Builder
    @Getter
    public static class StoreStatus {
        private String storeName;
        private Long warnedCount;
        private Long successCount;
        private Long failCount;
        private Double successRate;
    }

    @Builder
    @Getter
    public static class StoreTurnover {
        private String storeName;
        private Float turnover;
        private Float optimalStockRate;
    }

    @Builder
    @Getter
    public static class OverstockTrend {
        private String label;
        private List<Long> data;
    }
}
