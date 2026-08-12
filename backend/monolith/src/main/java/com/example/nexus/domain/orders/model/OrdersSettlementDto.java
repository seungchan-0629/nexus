package com.example.nexus.domain.orders.model;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.model.PageResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class OrdersSettlementDto {
    // 최종 응답: 상단 요약 + 하단 페이징 리스트
    @Getter
    @Builder
    public static class TotalOrderSettlementRes {
        private Long totalAmount;
        private Long expectedPayAmount;
        private PageResponse<OrderSettlementItemRes> orderHistory;
    }

    // 발주 정산 리스트 단건
    @Getter
    @Builder
    public static class OrderSettlementItemRes {
        private Long ordersIdx;
        private LocalDate createdDate;
        private String productNames;
        private Integer totalCount;
        private Long price;
        private OrdersStatus ordersStatus;
    }
}
