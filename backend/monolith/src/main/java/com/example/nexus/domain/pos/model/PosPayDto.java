package com.example.nexus.domain.pos.model;

import com.example.nexus.common.enums.PosPayMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class PosPayDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PayReq {
        @NotNull
        @Schema(example = "CARD", description = "결제 수단 (CARD / CASH)")
        private PosPayMethod method;

        @NotEmpty
        @Valid
        @Schema(description = "주문 항목 목록 (1개 이상 필수)")
        private List<PayLineReq> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PayLineReq {
        @NotNull
        @Schema(example = "10", description = "메뉴 ID")
        private Long menuIdx;

        @NotNull
        @Min(1)
        @Schema(example = "5", description = "주문 수량 (1 이상)")
        private Integer quantity;
    }

    @Getter
    @Builder
    public static class PayRes {
        private Long posPayIdx;
        private Long storeIdx;
        private PosPayMethod method;
        private Long payAmount;
        private LocalDateTime paidAt;
        private List<PayLineRes> items;
    }

    @Getter
    @Builder
    public static class PayLineRes {
        private Long menuIdx;
        private String menuName;
        private Integer quantity;
        private Integer unitPrice;
        private Long lineAmount;
    }

    @Getter
    @Builder
    public static class TodayPayRes {
        private Long posPayIdx;
        private PosPayMethod method;
        private Long payAmount;
        private LocalDateTime paidAt;
        private List<TodayPayLineRes> items;
    }

    @Getter
    @Builder
    public static class TodayPayLineRes {
        private Long menuIdx;
        private String menuName;
        private Integer quantity;
    }
}
