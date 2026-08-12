package com.example.nexus.domain.pos.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class PosCloseDto {

    @Getter
    @Builder
    public static class CloseRes {
        private Long storeIdx;

        private int processedPayCount;

        private int deductedProductKinds;

        private LocalDateTime closedAt;

        private String message;
    }
}
