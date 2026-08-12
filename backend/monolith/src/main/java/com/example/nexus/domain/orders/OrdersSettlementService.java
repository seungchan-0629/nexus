package com.example.nexus.domain.orders;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.orders.model.Orders;
import com.example.nexus.domain.orders.model.OrdersItem;
import com.example.nexus.domain.orders.model.OrdersSettlementDto;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersSettlementService {
    private final StoreRepository storeRepository;
    private final OrdersSettlementRepository ordersSettlementRepository;
    private final OrdersItemRepository ordersItemRepository;

    @Transactional(readOnly = true)
    public OrdersSettlementDto.TotalOrderSettlementRes getOrderSettlement(Long userIdx, int year, int month, int page, int size) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime end = start.plusMonths(1);

        // 총 납부액 (해당 월 전체 발주 합계)
        Long totalAmount = ordersSettlementRepository.sumTotalAmountByMonth(store.getIdx(), start, end);

        // 납부 예상 금액 (APPROVE 상태만)
        Long expectedPayAmount = ordersSettlementRepository.sumExpectedAmountByMonth(store.getIdx(), start, end);

        // 발주 내역 페이징 조회
        Page<Orders> ordersPage = ordersSettlementRepository.findOrderHistoryByMonth(
                store.getIdx(), start, end, PageRequest.of(page, size)
        );

        // 발주 idx 목록으로 ordersItem 한 번에 조회 (N+1 방지)
        List<Long> ordersIdxList = ordersPage.getContent().stream()
                .map(Orders::getIdx)
                .toList();

        Map<Long, List<OrdersItem>> itemMap;
        if (ordersIdxList.isEmpty()) itemMap = Map.of();
        else itemMap = ordersItemRepository.findByOrdersIdxIn(ordersIdxList)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getOrders().getIdx()));

        List<OrdersSettlementDto.OrderSettlementItemRes> content = ordersPage.getContent().stream()
                .map(order -> {
                    List<OrdersItem> items = itemMap.getOrDefault(order.getIdx(), List.of());

                    // 제품명 " + " 로 이어붙이기
                    String productNames = items.stream()
                            .map(item -> item.getProduct().getProductName())
                            .collect(Collectors.joining(" , "));

                    // 수량 합계
                    int totalCount = items.stream()
                            .mapToInt(OrdersItem::getCount)
                            .sum();

                    return OrdersSettlementDto.OrderSettlementItemRes.builder()
                            .ordersIdx(order.getIdx())
                            .createdDate(order.getCreatedAt().toLocalDate())
                            .productNames(productNames)
                            .totalCount(totalCount)
                            .price(order.getPrice())
                            .ordersStatus(order.getOrdersStatus())
                            .build();
                })
                .toList();

        PageResponse<OrdersSettlementDto.OrderSettlementItemRes> orderHistory = PageResponse.<OrdersSettlementDto.OrderSettlementItemRes>builder()
                .content(content)
                .number(ordersPage.getNumber())
                .size(ordersPage.getSize())
                .totalPages(ordersPage.getTotalPages())
                .totalElements(ordersPage.getTotalElements())
                .build();

        return OrdersSettlementDto.TotalOrderSettlementRes.builder()
                .totalAmount(totalAmount != null ? totalAmount : 0L)
                .expectedPayAmount(expectedPayAmount != null ? expectedPayAmount : 0L)
                .orderHistory(orderHistory)
                .build();
    }
}
