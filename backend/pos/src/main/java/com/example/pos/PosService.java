package com.example.pos;

import com.example.pos.common.enums.InventoryStatus;
import com.example.pos.common.exception.BaseException;
import com.example.pos.common.model.BaseResponseStatus;
import com.example.pos.common.model.PageResponse;
import com.example.pos.domain.menu.MenuItemRepository;
import com.example.pos.domain.menu.MenuRepository;
import com.example.pos.domain.menu.model.Menu;
import com.example.pos.domain.menu.model.MenuItem;
import com.example.pos.domain.store.StoreInventoryRepository;
import com.example.pos.domain.store.StoreRepository;
import com.example.pos.domain.store.model.Store;
import com.example.pos.domain.store.model.StoreInventory;
import com.example.pos.event.PaymentEvent;
import com.example.pos.event.PaymentEventItem;
import com.example.pos.model.*;
import com.example.pos.model.PosStoreInventoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.pos.model.PosStoreInventory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PosService {

    private record MenuOrderLine(Menu menu, int quantity) {}
    private final PosStoreInventoryRepository posStoreInventoryRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final PosPayRepository posPayRepository;
    private final PosOrdersItemRepository posOrdersItemRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public PageResponse<PosStoreInventoryDto.ListRes> listByUserIdxPaged(Long idx, int page, int size) {
        Store store = storeRepository.findByUserIdx(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        Page<Long> productPage =
                posStoreInventoryRepository.findPagedProductIdsByStoreIdx(store.getIdx(), PageRequest.of(page, size));

        List<Long> productIds = productPage.getContent();

        List<PosStoreInventory> lots = posStoreInventoryRepository.findByStoreIdxAndProductIds(store.getIdx(), productIds);

        Map<Long, Integer> productOrder = new HashMap<>();

        for (int i = 0; i < productIds.size(); i++) {
            productOrder.put(productIds.get(i), i);
        }

        List<PosStoreInventoryDto.ListRes> content = lots.stream()
                .map(PosStoreInventoryDto.ListRes::from)
                .sorted(Comparator
                        .comparingInt((PosStoreInventoryDto.ListRes dto) -> productOrder.getOrDefault(dto.getProductIdx(), Integer.MAX_VALUE))
                        .thenComparing(PosStoreInventoryDto.ListRes::getManufacturedDate))
                .toList();

        return PageResponse.<PosStoreInventoryDto.ListRes>builder()
                .content(content)
                .number(productPage.getNumber())
                .size(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .build();
    }

    public PosStoreInventoryDto.SyncCountRes changeCount(Long userIdx, Long posStoreInventoryIdx, Integer count) {
        Store myStore = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        PosStoreInventory posInventory = posStoreInventoryRepository.findById(posStoreInventoryIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POS_INVENTORY_NOT_FOUND));
        StoreInventory hqInventory = storeInventoryRepository.findById(posStoreInventoryIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_INVENTORY_NOT_FOUND));

        if (!posInventory.getStore().getIdx().equals(myStore.getIdx())) {
            throw new BaseException(BaseResponseStatus.STORE_INVENTORY_NOT_AUTHORIZED);
        }

        posInventory.setCount(count);
        posInventory.setStatus(resolveStatus(count, posInventory.getProduct().getMinStock()));
        hqInventory.setCount(count);
        hqInventory.setStatus(resolveStatus(count, hqInventory.getProduct().getMinStock()));

        PosStoreInventory posSaved = posStoreInventoryRepository.save(posInventory);
        StoreInventory hqSaved = storeInventoryRepository.save(hqInventory);

        return PosStoreInventoryDto.SyncCountRes.from(posSaved, hqSaved);
    }

    @Transactional(readOnly = true)
    public List<PosPayDto.TodayPayRes> listTodayPayHistory(Long userIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        List<PosPay> pays = posPayRepository.findByStoreIdxAndPaidAtBetweenOrderByPaidAtDesc(store.getIdx(), from, to);
        if (pays.isEmpty()) {
            return List.of();
        }

        List<Long> payIdxList = pays.stream().map(PosPay::getIdx).toList();
        List<PosOrdersItem> rows = posOrdersItemRepository.findByPosPay_IdxIn(payIdxList);

        Map<Long, List<PosPayDto.TodayPayLineRes>> itemsByPayIdx = rows.stream().collect(Collectors.groupingBy(
                r -> r.getPosPay().getIdx(),
                Collectors.mapping(
                        r -> PosPayDto.TodayPayLineRes.builder()
                                .menuIdx(r.getMenu().getIdx())
                                .menuName(r.getMenu().getMenuName())
                                .quantity(r.getQuantity())
                                .build(),
                        Collectors.toList()
                )
        ));

        return pays.stream().map(pay -> PosPayDto.TodayPayRes.builder()
                .posPayIdx(pay.getIdx())
                .method(pay.getMethod())
                .payAmount(pay.getPayAmount())
                .paidAt(pay.getPaidAt())
                .items(itemsByPayIdx.getOrDefault(pay.getIdx(), Collections.emptyList()))
                .build()).toList();
    }

    @Transactional
    public PosCloseDto.CloseRes deductOnClose(Long userIdx) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        List<PosPay> pending = posPayRepository
                .findByStoreIdxAndPaidAtBetweenAndStoreInventoryDeductedAtIsNullOrderByPaidAtAsc(store.getIdx(), from, to);

        if (pending.isEmpty()) {
            LocalDateTime closedAt = LocalDateTime.now();
            return PosCloseDto.CloseRes.builder()
                    .storeIdx(store.getIdx())
                    .processedPayCount(0)
                    .deductedProductKinds(0)
                    .closedAt(closedAt)
                    .message("당일 마감할 미반영 결제가 없습니다.")
                    .build();
        }

        List<Long> payIds = pending.stream().map(PosPay::getIdx).toList();
        List<PosOrdersItem> orderLines = posOrdersItemRepository.findByPosPay_IdxIn(payIds);

        Map<Long, Integer> menuQty = new HashMap<>();
        for (PosOrdersItem oi : orderLines) {
            menuQty.merge(oi.getMenu().getIdx(), oi.getQuantity(), Integer::sum);
        }

        List<MenuOrderLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : menuQty.entrySet()) {
            Menu menu = menuRepository.findById(e.getKey())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_MENU));
            lines.add(new MenuOrderLine(menu, e.getValue()));
        }

        Map<Long, Integer> productNeed = aggregateProductNeedFromRecipes(lines);
        for (Map.Entry<Long, Integer> e : productNeed.entrySet()) {
            deductOfficialFifo(store.getIdx(), e.getKey(), e.getValue());
        }

        LocalDateTime closedAt = LocalDateTime.now();
        for (PosPay pay : pending) {
            pay.setStoreInventoryDeductedAt(closedAt);
        }
        posPayRepository.saveAll(pending);

        String message = String.format(
                "당일 결제 %d건 기준으로 본사 재고에 %d종 원자재를 차감했습니다.",
                pending.size(),
                productNeed.size());

        eventPublisher.publishEvent(
            new com.example.pos.event.PosCloseDomainEvent(new com.example.pos.event.PosCloseEvent(userIdx, store.getIdx(), productNeed))
        );

        return PosCloseDto.CloseRes.builder()
                .storeIdx(store.getIdx())
                .processedPayCount(pending.size())
                .deductedProductKinds(productNeed.size())
                .closedAt(closedAt)
                .message(message)
                .build();
    }

    @Transactional
    public PosPayDto.PayRes pay(Long userIdx, PosPayDto.PayReq req) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        List<MenuOrderLine> lines = new ArrayList<>();

        for (PosPayDto.PayLineReq line : req.getItems()) {
            Menu menu = menuRepository.findById(line.getMenuIdx())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_MENU));

            if (menu.isDeleted()) {
                throw new BaseException(BaseResponseStatus.NOT_FOUND_MENU);
            }
            lines.add(new MenuOrderLine(menu, line.getQuantity()));
        }

        Map<Long, Integer> productNeedByProductIdx = aggregateProductNeedFromRecipes(lines);

        for (Map.Entry<Long, Integer> e : productNeedByProductIdx.entrySet()) {
            deductProductFromLotsFifo(store, e.getKey(), e.getValue());
        }

        List<PosPayDto.PayLineRes> lineResList = new ArrayList<>();

        long payAmount = 0L;

        for (MenuOrderLine row : lines) {
            long lineAmount = (long) row.menu().getPrice() * row.quantity();
            payAmount += lineAmount;
            lineResList.add(PosPayDto.PayLineRes.builder()
                    .menuIdx(row.menu().getIdx())
                    .menuName(row.menu().getMenuName())
                    .quantity(row.quantity())
                    .unitPrice(row.menu().getPrice())
                    .lineAmount(lineAmount)
                    .build());
        }

        PosPay posPay = PosPay.builder()
                .method(req.getMethod())
                .paidAt(LocalDateTime.now())
                .payAmount(payAmount)
                .store(store)
                .build();

        PosPay savedPay = posPayRepository.save(posPay);

        List<PosOrdersItem> orderLines = new ArrayList<>();

        for (MenuOrderLine row : lines) {
            orderLines.add(PosOrdersItem.builder()
                    .menu(row.menu())
                    .posPay(savedPay)
                    .quantity(row.quantity())
                    .build());
        }
        posOrdersItemRepository.saveAll(orderLines);

        // Kafka 이벤트 발행
        com.example.pos.event.PaymentEvent event = buildPaymentEvent(savedPay, store, lines);
        eventPublisher.publishEvent(new com.example.pos.event.PaymentDomainEvent(event));

        return PosPayDto.PayRes.builder()
                .posPayIdx(savedPay.getIdx())
                .storeIdx(store.getIdx())
                .method(savedPay.getMethod())
                .payAmount(savedPay.getPayAmount())
                .paidAt(savedPay.getPaidAt())
                .items(lineResList)
                .build();
    }

    private com.example.pos.event.PaymentEvent buildPaymentEvent(PosPay savedPay, Store store, List<MenuOrderLine> lines) {
        List<com.example.pos.event.PaymentEventItem> items = lines.stream()
                .map(row -> new com.example.pos.event.PaymentEventItem(
                        row.menu().getIdx(),
                        row.menu().getMenuName(),
                        null, // POS 메뉴는 카테고리 식별자(idx)가 없으므로 null
                        row.menu().getMenuCategoryName(),
                        row.menu().getPrice(),
                        row.quantity(),
                        (long) row.menu().getPrice() * row.quantity()
                ))
                .toList();

        return new com.example.pos.event.PaymentEvent(
                savedPay.getIdx(),
                store.getIdx(),
                store.getStoreName(),
                savedPay.getMethod().name(),       // PosPayMethod enum → String
                savedPay.getPaidAt(),
                savedPay.getPayAmount(),
                items
        );
    }

    private Map<Long, Integer> aggregateProductNeedFromRecipes(List<MenuOrderLine> lines) {
        Map<Long, Integer> productNeed = new HashMap<>();
        for (MenuOrderLine row : lines) {
            List<MenuItem> recipe = menuItemRepository.findByMenu_Idx(row.menu().getIdx());

            for (MenuItem mi : recipe) {
                Long productIdx = mi.getProduct().getIdx();
                int need = mi.getQuantity() * row.quantity();
                productNeed.merge(productIdx, need, Integer::sum);
            }
        }
        return productNeed;
    }

    private void deductProductFromLotsFifo(Store store, Long productIdx, int amount) {
        if (amount <= 0) {
            return;
        }
        List<PosStoreInventory> lots = posStoreInventoryRepository
                .findByStoreAndProductForUpdate(store.getIdx(), productIdx);
        int availableTotal = lots.stream().mapToInt(PosStoreInventory::getCount).sum();
        if (availableTotal < amount) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("productIdx", productIdx);
            detail.put("required", amount);
            detail.put("availableTotal", availableTotal);
            throw new BaseException(BaseResponseStatus.POS_STORE_INVENTORY_INSUFFICIENT, detail);
        }
        int remaining = amount;
        for (PosStoreInventory lot : lots) {
            if (remaining <= 0) {
                break;
            }
            int onHand = lot.getCount();
            if (onHand <= 0) {
                continue;
            }
            int take = Math.min(onHand, remaining);
            lot.setCount(onHand - take);
            remaining -= take;
        }
    }

    private void deductOfficialFifo(Long storeIdx, Long productIdx, int amount) {
        if (amount <= 0) {
            return;
        }
        List<StoreInventory> lots = storeInventoryRepository
                .findByStore_IdxAndProduct_IdxOrderByManufacturedDateAsc(storeIdx, productIdx);
        int availableTotal = lots.stream().mapToInt(StoreInventory::getCount).sum();
        if (availableTotal < amount) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("productIdx", productIdx);
            detail.put("required", amount);
            detail.put("availableTotal", availableTotal);
            throw new BaseException(BaseResponseStatus.STORE_INVENTORY_INSUFFICIENT, detail);
        }
        int remaining = amount;
        for (StoreInventory lot : lots) {
            if (remaining <= 0) {
                break;
            }
            int onHand = lot.getCount();
            if (onHand <= 0) {
                continue;
            }
            int take = Math.min(onHand, remaining);
            lot.setCount(onHand - take);
            lot.setStatus(resolveStatus(lot.getCount(), lot.getProduct().getMinStock()));
            remaining -= take;
        }
    }

    private InventoryStatus resolveStatus(int count, int minStock) {
        if (count <= 0) return InventoryStatus.CRITICAL;
        if (count <= minStock / 2) return InventoryStatus.CRITICAL;
        if (count <= minStock) return InventoryStatus.LOW;
        return InventoryStatus.NORMAL;
    }
}
