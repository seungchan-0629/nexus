package com.example.pos.model;

import com.example.pos.common.enums.PosPayMethod;
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
        private PosPayMethod method;

        @NotEmpty
        @Valid
        private List<PayLineReq> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PayLineReq {
        @NotNull
        private Long menuIdx;

        @NotNull
        @Min(1)
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
