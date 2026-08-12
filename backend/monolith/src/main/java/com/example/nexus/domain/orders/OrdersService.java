package com.example.nexus.domain.orders;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.inventory.InventoryMovementService;
import com.example.nexus.domain.inventory.model.InventoryMovementDto;
import com.example.nexus.domain.delivery.DeliveryService;
import com.example.nexus.domain.head.HeadIncomeRepository;
import com.example.nexus.domain.head.model.HeadIncome;
import com.example.nexus.domain.notification.HeadNotificationService;
import com.example.nexus.domain.notification.StoreNotificationService;
import com.example.nexus.domain.orders.model.*;
import com.example.nexus.domain.product.ProductRepository;
import com.example.nexus.domain.product.model.Product;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.store.model.StoreInventory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final DangerRepository dangerRepository;
    private final OrdersRepository ordersRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;
    private final HeadNotificationService headNotificationService;
    private final StoreNotificationService storeNotificationService;
    private final DeliveryService deliveryService;
    private final HeadIncomeRepository headIncomeRepository;

    // 자동 발주 제안 검색 조회 (AUTO + WAITING 상태 대상)
    // 매장명 키워드 검색 + 페이징 처리
    public Page<OrdersDto.OrderListRes> findAllAuto(String keyword, Pageable pageable) {
        Specification<Orders> spec = OrdersSpecification.ordersTypeEquals(OrdersType.AUTO)
                .and(OrdersSpecification.statusIn(List.of(OrdersStatus.WAITING)));

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(OrdersSpecification.keywordLike(keyword));
        }

        return ordersRepository.findAll(spec, pageable).map(OrdersDto.OrderListRes::from);
    }

    // 확정 발주 검색 조회 (CONFIRMED 상태 대상)
    // 매장명 키워드 검색 + 페이징 처리
    public Page<OrdersDto.OrderListRes> findAllConfirmed(String keyword, Pageable pageable) {
        Specification<Orders> spec = OrdersSpecification.statusIn(List.of(OrdersStatus.CONFIRMED));

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(OrdersSpecification.keywordLike(keyword));
        }

        return ordersRepository.findAll(spec, pageable).map(OrdersDto.OrderListRes::from);
    }

    // 발주 이력 검색 조회 (APPROVE, REJECT, CANCELLED 상태 대상)
    // 발주 유형, 기간, 키워드 조건으로 필터링 + 페이징 처리
    public Page<OrdersDto.OrderListRes> findOrderHistory(OrdersType ordersType, LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        Specification<Orders> spec = OrdersSpecification.statusIn(
                List.of(OrdersStatus.APPROVE, OrdersStatus.REJECT, OrdersStatus.CANCELLED));

        if (ordersType != null) {
            spec = spec.and(OrdersSpecification.ordersTypeEquals(ordersType));
        }
        if (startDate != null) {
            spec = spec.and(OrdersSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(OrdersSpecification.createdBefore(endDate));
        }
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(OrdersSpecification.keywordLike(keyword));
        }

        return ordersRepository.findAll(spec, pageable).map(OrdersDto.OrderListRes::from);
    }

    // 이상 발주 검색 조회 (isDanger=true 대상)
    // 기간, 키워드 조건으로 필터링 + 페이징 처리
    // 각 발주에 대해 같은 매장의 최근 N개월 평균 수량 계산
    public Page<DangerDto.DangerListRes> findDangerOrders(LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        Specification<Orders> spec = OrdersSpecification.isDangerTrue()
                .and(OrdersSpecification.statusIn(List.of(
                        OrdersStatus.CONFIRMED, OrdersStatus.APPROVE, OrdersStatus.REJECT)));

        if (startDate != null) {
            spec = spec.and(OrdersSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(OrdersSpecification.createdBefore(endDate));
        }
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(OrdersSpecification.keywordLike(keyword));
        }

        int period = dangerRepository.findById(1L)
                .map(Danger::getPeriod).orElse(3);

        Page<Orders> page = ordersRepository.findAll(spec, pageable);
        List<Long> orderIds = page.getContent().stream().map(Orders::getIdx).toList();

        Map<Long, int[]> dangerStatsMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<Object[]> rows = ordersRepository.findAvgQtyBatch(orderIds, period);
            for (Object[] row : rows) {
                Long ordersIdx = ((Number) row[0]).longValue();
                int avgQty = ((Number) row[1]).intValue();
                int totalQty = ((Number) row[2]).intValue();
                dangerStatsMap.put(ordersIdx, new int[]{avgQty, totalQty});
            }
        }

        return page.map(orders -> {
            int[] stats = dangerStatsMap.getOrDefault(orders.getIdx(), new int[]{0, 0});
            return DangerDto.DangerListRes.from(orders, stats[0], stats[1]);
        });
    }

    // 발주 상세 조회 (단건)
    public OrdersDto.OrdersRes findById(Long ordersIdx) {
        Orders orders = ordersRepository.findByIdWithDetails(ordersIdx).orElseThrow(
                () -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));
        return OrdersDto.OrdersRes.from(orders);
    }

    // 이상 발주 기준 설정 조회
    public DangerDto.DangerRes find() {
        return dangerRepository.findById(1L)
                .map(DangerDto.DangerRes::from)
                .orElse(DangerDto.DangerRes.builder()
                        .ratio(200)
                        .period(3)
                        .build());
    }

    // 이상 발주 기준 설정 저장 및 기존 이상 발주 재평가
    @Transactional
    public void save(DangerDto.DangerReq req) {
        Danger danger = dangerRepository.findById(1L)
                .orElse(Danger.builder().build());

        danger.update(req.getRatio(), req.getPeriod());

        dangerRepository.save(danger);

        // 기존 이상 발주 재평가: 새 기준 미달 시 isDanger 해제
        List<Orders> dangerOrders = ordersRepository.findAllDangerWithDetails();

        if (!dangerOrders.isEmpty()) {
            List<Long> orderIds = dangerOrders.stream().map(Orders::getIdx).toList();
            List<Object[]> rows = ordersRepository.findAvgQtyBatch(orderIds, req.getPeriod());
            Map<Long, Integer> avgQtyMap = new HashMap<>();
            for (Object[] row : rows) {
                avgQtyMap.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
            }

            for (Orders orders : dangerOrders) {
                List<OrdersItem> items = orders.getOrdersItemList() != null ? orders.getOrdersItemList() : Collections.emptyList();
                int totalQty = items.stream().mapToInt(OrdersItem::getCount).sum();
                int avgQty = avgQtyMap.getOrDefault(orders.getIdx(), 0);
                int ratio = avgQty > 0 ? (totalQty - avgQty) * 100 / avgQty : 0;

                if (ratio < req.getRatio()) {
                    orders.markDanger(false);
                }
            }
        }
    }

    // 본사 - 이상 발주 개별 승인 (출고 처리 포함)
    @Transactional
    public void approve(Long ordersIdx) {
        Orders orders = ordersRepository.findByIdWithDetailsForUpdate(ordersIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));

        if (orders.getOrdersStatus() != OrdersStatus.CONFIRMED) {
            throw new BaseException(BaseResponseStatus.ORDERS_INVALID_STATUS);
        }

        applyOutboundForOrder(orders, "발주 승인(이상) ordersIdx=");
        orders.approve();
        createHeadIncome(orders);
        deliveryService.startDeliveryByOrders(orders);
    }

    // 본사 - 이상 발주 개별 반려
    @Transactional
    public void reject(Long ordersIdx) {
        Orders orders = ordersRepository.findByIdForUpdate(ordersIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));

        if (orders.getOrdersStatus() != OrdersStatus.CONFIRMED) {
            throw new BaseException(BaseResponseStatus.ORDERS_INVALID_STATUS);
        }

        orders.reject();
    }

    // 본사 - 확정 발주 일괄 승인 (출고 처리 포함)
    @Transactional
    public void approveAllConfirmed() {
        List<Orders> confirmedOrders = ordersRepository.findAllByOrdersStatusWithDetails(OrdersStatus.CONFIRMED);

        for (Orders orders : confirmedOrders) {
            try {
                applyOutboundForOrder(orders, "발주 일괄승인 ordersIdx=");
            } catch (BaseException e) {
                if (e.getStatus() == BaseResponseStatus.STORE_INVENTORY_INSUFFICIENT) {
                    throw new BaseException(BaseResponseStatus.ORDERS_APPROVE_INSUFFICIENT_STOCK);
                }
                throw e;
            }
            orders.approve();
            createHeadIncome(orders);
            deliveryService.startDeliveryByOrders(orders);
        }
    }

    // 발주 승인 시 매출채권(HeadIncome) 생성
    private void createHeadIncome(Orders orders) {
        HeadIncome headIncome = new HeadIncome();
        headIncome.setPrice(orders.getPrice());
        headIncome.setStatus(false);
        headIncome.setSettlementIdx(null);
        headIncome.setStore(orders.getStore());
        headIncome.setOrders(orders);
        headIncomeRepository.save(headIncome);
    }

    // 발주 승인 시 품목별 출고 처리 (재고 차감)
    private void applyOutboundForOrder(Orders orders, String memoPrefix) {
        Long storeIdx = orders.getStore().getIdx();
        List<OrdersItem> items = orders.getOrdersItemList();
        if (items == null || items.isEmpty()) {
            throw new BaseException(BaseResponseStatus.ORDERS_ITEMS_EMPTY);
        }
        String memo = memoPrefix + orders.getIdx();
        for (OrdersItem item : items) {
            InventoryMovementDto.OutboundReq outboundReq = new InventoryMovementDto.OutboundReq(
                    item.getProduct().getIdx(),
                    storeIdx,
                    item.getCount(),
                    memo
            );
            inventoryMovementService.outbound(outboundReq);
        }
    }

    /**
     * 이상 발주 판정
     * 해당 매장의 최근 N개월 발주 건당 평균 수량 대비
     * 주문 수량의 초과 비율이 기준 이상이면 이상 발주로 판정
     * 예: 평균 10개, 이번 30개, 기준 200% → (30-10)*100/10 = 200% → 이상 발주
     */
    private List<OrdersItem> recalculatePrice(Orders orders) {
        List<OrdersItem> items = ordersItemRepository.findByOrdersIdx(orders.getIdx());
        long total = items.stream()
                .mapToLong(item -> (long) item.getProduct().getUnitPrice() * item.getCount())
                .sum();
        orders.updatePrice(total);
        return items;
    }

    private boolean evaluateDanger(Long storeIdx, int totalQty, LocalDateTime baseTime, Long excludeIdx) {
        Danger danger = dangerRepository.findById(1L)
                .orElse(Danger.builder().ratio(200).period(3).build());
        int dangerRatio = danger.getRatio();
        int period = danger.getPeriod();

        LocalDateTime since = baseTime.minusMonths(period);
        Integer avgQty = ordersRepository.findAvgQtyByStoreAndPeriod(storeIdx, since, excludeIdx);
        return avgQty > 0 && (totalQty - avgQty) * 100 / avgQty >= dangerRatio;
    }

    @Transactional
    public void createStoreManualOrder(Long userIdx, OrdersDto.OrdersReq req) {
        // 1. 주문 아이템 리스트 검증
        if (req.getOrdersItemList() == null || req.getOrdersItemList().isEmpty()) {
            throw new BaseException(BaseResponseStatus.ORDERS_ITEMS_EMPTY);
        }

        // 2. 인증 정보로 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 3. Product 조회 및 총 가격 계산
        long totalPrice = 0;
        List<OrdersItem> itemList = new ArrayList<>();

        for (OrdersItemDto.OrdersItemReq itemReq : req.getOrdersItemList()) {
            Product product = productRepository.findById(itemReq.getProductIdx())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_PRODUCT));

            totalPrice += (long) product.getUnitPrice() * itemReq.getCount();

            itemList.add(OrdersItem.builder()
                    .count(itemReq.getCount())
                    .product(product)
                    .build());
        }

        // 4. 이상 발주 판정
        int totalQty = itemList.stream().mapToInt(OrdersItem::getCount).sum();
        boolean isDanger = evaluateDanger(store.getIdx(), totalQty, LocalDateTime.now(), null);

        // 이상 발주로 판정되면 본사에 알림 발송
        if (isDanger) {
            headNotificationService.create(
                    NotificationType.ABNORMAL_ORDER,
                    "비정상 발주 감지 - " + store.getStoreName(),
                    "수동 발주 수량 " + totalQty + "개 (평균 대비 초과)");

            // 가맹점 비정상 발주 재확인 요청 알림 (NOTIFY_010)
            storeNotificationService.create(
                    NotificationType.ABNORMAL_ORDER,
                    "발주 수량 재확인 요청",
                    "발주 수량 " + totalQty + "개가 평균 대비 비정상으로 감지되었습니다. 발주 내역을 확인해주세요.",
                    store);
        }

        // 5. Orders 저장
        Orders orders = ordersRepository.save(Orders.builder()
                .price(totalPrice)
                .ordersType(OrdersType.MANUAL)
                .ordersStatus(OrdersStatus.CONFIRMED)
                .isDanger(isDanger)
                .createdAt(LocalDateTime.now())
                .store(store)
                .build());

        // 6. OrdersItem 저장
        for (OrdersItem item : itemList) {
            ordersItemRepository.save(OrdersItem.builder()
                    .count(item.getCount())
                    .product(item.getProduct())
                    .orders(orders)
                    .build());
        }

        // 7. 배송 생성 (READY 상태)
        deliveryService.createDelivery(orders);
    }

    // 점주 - 제안 발주서 목록 조회 (WAITING 상태, 현재 재고 포함)
    public List<OrdersDto.OrdersRes> findByUserIdxAndOrdersStatus(Long userIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        List<StoreInventory> inventoryList = storeInventoryRepository.findByStoreIdx(store.getIdx());
        Map<Long, Integer> stockMap = new HashMap<>();
        for (StoreInventory inv : inventoryList) {
            Long productIdx = inv.getProduct().getIdx();
            if (stockMap.containsKey(productIdx)) {
                stockMap.put(productIdx, stockMap.get(productIdx) + inv.getCount());
            } else {
                stockMap.put(productIdx, inv.getCount());
            }
        }

        return ordersRepository.findPendingWithDetails(store.getIdx(), OrdersStatus.WAITING, OrdersType.AUTO).stream()
                .map(order -> OrdersDto.OrdersRes.fromWithStock(order, stockMap))
                .toList();
    }

    // 점주 - 제안 발주서 확정 (이상 발주 재판정 포함)
    @Transactional
    public void confirmStoreOrder(Long userIdx, Long ordersIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        Orders orders = ordersRepository.findByIdWithDetailsForUpdate(ordersIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));

        if (!orders.getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.ORDERS_NOT_AUTHORIZED);
        }

        if (orders.getOrdersStatus() != OrdersStatus.WAITING) {
            throw new BaseException(BaseResponseStatus.ORDERS_INVALID_STATUS);
        }

        // 점주가 아이템을 수정했을 수 있으므로 확정 시점에 가격 재계산
        List<OrdersItem> items = recalculatePrice(orders);

        // 이상 발주 재판정
        int totalQty = items.stream().mapToInt(OrdersItem::getCount).sum();
        boolean isDanger = evaluateDanger(store.getIdx(), totalQty, orders.getCreatedAt(), orders.getIdx());
        orders.markDanger(isDanger);

        // 이상 발주로 판정되면 본사에 알림 발송
        if (isDanger) {
            headNotificationService.create(
                    NotificationType.ABNORMAL_ORDER,
                    "비정상 발주 감지 - " + store.getStoreName(),
                    "발주 확정 수량 " + totalQty + "개 (평균 대비 초과)");

            // 가맹점 비정상 발주 재확인 요청 알림 (NOTIFY_010)
            storeNotificationService.create(
                    NotificationType.ABNORMAL_ORDER,
                    "발주 수량 재확인 요청",
                    "발주 확정 수량 " + totalQty + "개가 평균 대비 비정상으로 감지되었습니다. 발주 내역을 확인해주세요.",
                    store);
        }

        orders.confirm();
        deliveryService.createDelivery(orders);
    }

    // 점주 - 제안 발주서에 품목 추가 (가격 자동 반영)
    @Transactional
    public void addStoreItem(Long userIdx, Long ordersIdx, OrdersItemDto.OrdersItemReq req) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        Orders orders = ordersRepository.findByIdForUpdate(ordersIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));

        if (!orders.getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.ORDERS_NOT_AUTHORIZED);
        }

        Product product = productRepository.findById(req.getProductIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_PRODUCT));

        ordersItemRepository.save(OrdersItem.builder()
                .count(req.getCount())
                .product(product)
                .orders(orders)
                .build());

        recalculatePrice(orders);
    }

    // 점주 - 제안 발주서 품목 수량 변경 (가격 차액 자동 반영)
    @Transactional
    public void updateStoreItemCount(Long userIdx, Long ordersItemIdx, Integer count) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        OrdersItem item = ordersItemRepository.findById(ordersItemIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS_ITEM));

        if (!item.getOrders().getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.ORDERS_NOT_AUTHORIZED);
        }

        Orders orders = ordersRepository.findByIdForUpdate(item.getOrders().getIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));
        item.updateCount(count);
        recalculatePrice(orders);
    }

    // 점주 - 제안 발주서 품목 삭제 (가격 자동 차감)
    @Transactional
    public void deleteStoreItem(Long userIdx, Long ordersItemIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        OrdersItem item = ordersItemRepository.findById(ordersItemIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS_ITEM));

        if (!item.getOrders().getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.ORDERS_NOT_AUTHORIZED);
        }

        Orders orders = ordersRepository.findByIdForUpdate(item.getOrders().getIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));
        ordersItemRepository.delete(item);
        ordersItemRepository.flush();
        recalculatePrice(orders);
    }

    // 점주 - 제안 발주서 거절
    @Transactional
    public void rejectStoreOrder(Long userIdx, Long ordersIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        Orders orders = ordersRepository.findById(ordersIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));

        if (!orders.getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.ORDERS_NOT_AUTHORIZED);
        }

        if (orders.getOrdersStatus() != OrdersStatus.WAITING) {
            throw new BaseException(BaseResponseStatus.ORDERS_INVALID_STATUS);
        }

        orders.reject();
    }

    // 점주 - 발주 이력 페이징 조회 (확정/승인/반려/취소)
    public Page<OrdersDto.OrdersRes> findByUserIdxPaged(Long userIdx, Pageable pageable) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        return ordersRepository.findAllByStore_IdxAndOrdersStatusIn(
                store.getIdx(),
                List.of(OrdersStatus.CONFIRMED, OrdersStatus.APPROVE, OrdersStatus.REJECT, OrdersStatus.CANCELLED),
                pageable
        ).map(OrdersDto.OrdersRes::from);
    }

    // 점주 - 확정 발주 취소
    @Transactional
    public void cancelOrder(Long userIdx, Long ordersIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        Orders orders = ordersRepository.findByIdForUpdate(ordersIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ORDERS));

        if (!orders.getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.ORDERS_NOT_AUTHORIZED);
        }

        if (orders.getOrdersStatus() != OrdersStatus.CONFIRMED) {
            throw new BaseException(BaseResponseStatus.ORDERS_INVALID_STATUS);
        }

        orders.cancel();
    }

    public List<Orders> findByStoreIdx(Long storeIdx) {
        return ordersRepository.findAllByStoreIdx(storeIdx);
    }

}
