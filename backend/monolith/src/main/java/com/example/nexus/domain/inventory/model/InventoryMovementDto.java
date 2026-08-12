package com.example.nexus.domain.inventory.model;

import com.example.nexus.common.enums.MovementLocationType;
import com.example.nexus.common.enums.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class InventoryMovementDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InboundReq {
        @Schema(example = "1", description = "입고할 제품 ID")
        private Long productIdx;

        @Schema(example = "100", description = "입고 수량")
        private Integer quantity;

        @Schema(example = "정기 입고", description = "입고 메모 (선택)")
        private String memo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundReq {
        @Schema(example = "1", description = "출고할 제품 ID")
        private Long productIdx;

        @Schema(example = "1", description = "배송 대상 매장 ID")
        private Long storeIdx;

        @Schema(example = "50", description = "출고 수량")
        private Integer quantity;

        @Schema(example = "매장 정기 배송", description = "출고 메모 (선택)")
        private String memo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovementRes {
        private Long movementIdx;
        private Long productIdx;
        private Integer quantity;
        private Integer headCount;
        private Long storeIdx;
        private Integer storeCount;

        public static MovementRes from(InventoryMovement movement) {
            return MovementRes.builder()
                    .movementIdx(movement.getIdx())
                    .productIdx(movement.getProduct().getIdx())
                    .quantity(movement.getQuantity())
                    .storeIdx(movement.getToRefIdx())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovementListRes {
        private Long movementIdx;
        private Long productIdx;
        private String productName;
        private MovementType movementType;
        private Integer quantity;
        private MovementLocationType fromLocationType;
        private Long fromRefIdx;
        private MovementLocationType toLocationType;
        private Long toRefIdx;
        private String memo;
        private LocalDateTime createdAt;

        public static MovementListRes from(InventoryMovement movement) {
            return MovementListRes.builder()
                    .movementIdx(movement.getIdx())
                    .productIdx(movement.getProduct().getIdx())
                    .productName(movement.getProduct().getProductName())
                    .movementType(movement.getMovementType())
                    .quantity(movement.getQuantity())
                    .fromLocationType(movement.getFromLocationType())
                    .fromRefIdx(movement.getFromRefIdx())
                    .toLocationType(movement.getToLocationType())
                    .toRefIdx(movement.getToRefIdx())
                    .memo(movement.getMemo())
                    .createdAt(movement.getCreatedAt())
                    .build();
        }
    }
}
