package com.example.nexus.domain.delivery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {

    @JsonProperty("deliveryIdx")
    private Long deliveryIdx;

    @JsonProperty("orderIdx")
    private Long orderIdx;

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("deliveryStatus")
    private String deliveryStatus;

    @JsonProperty("delayReason")
    private String delayReason;

    @JsonProperty("departureDate")
    private LocalDateTime departureDate;

    @JsonProperty("estimatedArrivalAt")
    private LocalDateTime estimatedArrivalAt;

    @JsonProperty("deliveredDate")
    private LocalDateTime deliveredDate;

    @JsonProperty("orderPrice")
    private Long orderPrice;

    @JsonProperty("itemCount")
    private Integer itemCount;

    public static DeliveryDto from(Delivery delivery) {

        return DeliveryDto.builder()
                .deliveryIdx(delivery.getIdx())
                .orderIdx(delivery.getOrders().getIdx())
                .storeName(delivery.getOrders().getStore().getStoreName())
                .deliveryStatus(delivery.getDeliveryStatus().name())
                .delayReason(delivery.getDelayReason())
                .departureDate(delivery.getDepartureDate())
                .estimatedArrivalAt(delivery.getEstimatedArrivalAt())
                .deliveredDate(delivery.getDeliveredDate())
                .orderPrice(delivery.getOrders().getPrice())
                .itemCount(delivery.getOrders().getOrdersItemList() != null ? delivery.getOrders().getOrdersItemList().size() : 0)
                .build();
    }

    // 페이징 응답 DTO
    @Getter
    @Builder
    public static class DeliveryPageRes {

        private List<DeliveryDto> deliveryList;
        private int totalPage;
        private long totalCount;
        private int currentPage;
        private int currentSize;

        public static DeliveryPageRes from(Page<Delivery> page) {

            return DeliveryPageRes.builder()
                    .deliveryList(
                            page.getContent()
                                    .stream()
                                    .map(DeliveryDto::from)
                                    .toList()
                    )
                    .totalPage(page.getTotalPages())
                    .totalCount(page.getTotalElements())
                    .currentPage(page.getNumber())
                    .currentSize(page.getSize())
                    .build();
        }
    }

    // 배송 지연 사유 요청 DTO
    @Getter
    @NoArgsConstructor
    @Schema(description = "배송 지연 사유 등록 요청")
    public static class DelayReasonRequest {

        @Schema(description = "배송 번호(IDX)", example = "1")
        private Long deliveryIdx;

        @Schema(description = "지연 사유", example = "기상 악화로 인한 배송 지연")
        private String delayReason;
    }
}