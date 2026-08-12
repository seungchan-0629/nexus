package com.example.nexus.domain.wastelog;


import com.example.nexus.domain.orders.OrdersRepository;
import com.example.nexus.domain.store.StoreInventoryRepository;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.pos.PosStoreInventoryRepository;
import com.example.nexus.domain.pos.model.PosStoreInventory;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;

import com.example.nexus.domain.store.model.StoreInventory;
import com.example.nexus.domain.wastelog.model.WasteLog;
import com.example.nexus.domain.wastelog.model.WasteLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WasteLogService {

    private final WasteLogRepository wasteLogRepository;
    private final StoreRepository storeRepository;
    private final PosStoreInventoryRepository posStoreInventoryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final OrdersRepository ordersRepository;

    public WasteLogDto.StatsRes getWasteStats() {
        YearMonth lastMonth = YearMonth.from(LocalDateTime.now()).minusMonths(1);
        YearMonth thisMonth = YearMonth.from(LocalDateTime.now());

        long lastMonthWasteSum = getInputMonthWasteSum(lastMonth);
        long thisMonthWasteSum = getInputMonthWasteSum(thisMonth);
        
        long lastMonthAmountSum = getInputMonthWasteAmountSum(lastMonth);
        long thisMonthAmountSum = getInputMonthWasteAmountSum(thisMonth);

        Float wasteGradient = lastMonthWasteSum > 0 ? (float) thisMonthWasteSum / lastMonthWasteSum : null;
        long wasteSavingAmount = Math.max(0, lastMonthAmountSum - thisMonthAmountSum);

        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(5).withDayOfMonth(1).toLocalDate().atStartOfDay();
        List<Object[]> monthlyTrendRaw = wasteLogRepository.findMonthlyWasteTrend(sixMonthsAgo);
        List<WasteLogDto.MonthlyTrend> monthlyTrend = monthlyTrendRaw.stream()
                .map(obj -> WasteLogDto.MonthlyTrend.builder()
                        .month((String) obj[0])
                        .quantity(obj[1] != null ? ((Number) obj[1]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());

        LocalDateTime thisMonthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime thisMonthEnd = thisMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Object[]> categoryRatioRaw = wasteLogRepository.findCategoryWasteRatio(thisMonthStart, thisMonthEnd);
        List<WasteLogDto.CategoryRatio> categoryRatio = categoryRatioRaw.stream()
                .map(obj -> WasteLogDto.CategoryRatio.builder()
                        .categoryName((String) obj[0])
                        .quantity(obj[1] != null ? ((Number) obj[1]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());

        List<Object[]> storeStatusRaw = wasteLogRepository.findStoreWasteStatus(thisMonthStart, thisMonthEnd);
        long totalWarned = 0;
        long totalSuccess = 0;
        List<WasteLogDto.StoreStatus> storeStatus = new ArrayList<>();
        
        for (Object[] obj : storeStatusRaw) {
            long total = obj[1] != null ? ((Number) obj[1]).longValue() : 0L;
            long manual = obj[2] != null ? ((Number) obj[2]).longValue() : 0L;
            long expired = obj[3] != null ? ((Number) obj[3]).longValue() : 0L;
            

            double rate = total > 0 ? (double) manual / total * 100 : 0.0;
            
            totalWarned += total;
            totalSuccess += manual;
            
            storeStatus.add(WasteLogDto.StoreStatus.builder()
                    .storeName((String) obj[0])
                    .warnedCount(total)
                    .successCount(manual)
                    .failCount(expired)
                    .successRate(rate)
                    .build());
        }

        float overallSuccessRate = totalWarned > 0 ? (float) totalSuccess / totalWarned * 100 : 0.0f;

        // ── 재고 효율성 지표 추가 ──────────────────────────────
        List<Store> allStores = storeRepository.findAll();
        List<WasteLogDto.StoreTurnover> turnoverTrend = new ArrayList<>();
        float totalTurnover = 0;
        float totalOptimalRate = 0;

        for (Store store : allStores) {
            // 1. 재고 회전율 (Turnover Rate) = 당월 출고량(발주승인수량) / 평균 재고
            long monthlySales = ordersRepository.sumApprovedPriceByStoreAndPeriod(
                    store.getIdx(), thisMonthStart, thisMonthEnd) / 5000; // 가상의 평균 단가로 수량 추정 또는 ordersItem 활용
            
            List<StoreInventory> storeInventories = storeInventoryRepository.findByStoreIdx(store.getIdx());
            double avgStock = storeInventories.stream()
                    .mapToInt(StoreInventory::getCount)
                    .average().orElse(1.0);
            
            float turnover = (float) (monthlySales / (avgStock > 0 ? avgStock : 1.0));
            totalTurnover += turnover;

            // 2. 적정 재고 유지율 (Optimal Stock Rate) = 적정 상태 품목 수 / 전체 품목 수
            long totalItems = storeInventories.size();
            long optimalItems = storeInventories.stream()
                    .filter(si -> si.getStatus() == InventoryStatus.NORMAL)
                    .count();
            float optimalRate = totalItems > 0 ? (float) optimalItems / totalItems * 100 : 0.0f;
            totalOptimalRate += optimalRate;

            turnoverTrend.add(WasteLogDto.StoreTurnover.builder()
                    .storeName(store.getStoreName())
                    .turnover(turnover)
                    .optimalStockRate(optimalRate)
                    .build());
        }

        float avgTurnover = allStores.isEmpty() ? 0 : totalTurnover / allStores.size();
        float avgOptimalStockRate = allStores.isEmpty() ? 0 : totalOptimalRate / allStores.size();

        return WasteLogDto.StatsRes.builder()
                .wasteSum(thisMonthWasteSum)
                .lastMonthWasteSum(lastMonthWasteSum)
                .wasteGradient(wasteGradient)
                .useSuccessRate(overallSuccessRate)
                .wasteSavingAmount(wasteSavingAmount)
                .avgTurnover(avgTurnover)
                .optimalStockRate(avgOptimalStockRate)
                .overstockCount(0)
                .autoOrderAccuracy(0.0f)
                .monthlyWasteTrend(monthlyTrend)
                .categoryWasteRatio(categoryRatio)
                .storeWasteStatus(storeStatus)
                .storeTurnoverTrend(turnoverTrend)
                .storeOverstockTrend(new ArrayList<>())
                .build();
    }

    @Transactional
    public List<WasteLogDto.RegRes> registerOverDueDateInventories(List<Long> idxList) {

        List<WasteLogDto.RegRes> resList = new ArrayList<>();

        for(Long idx : idxList) {
            StoreInventory inventory = storeInventoryRepository.findById(idx).orElse(null);
            if (inventory == null || inventory.getCount() <= 0) continue;

            int quantity = inventory.getCount();
            long amountLoss = (long) quantity * inventory.getProduct().getUnitPrice();

            WasteLog entity = WasteLog.builder()
                    .quantity(quantity)
                    .amountLoss((int) amountLoss)
                    .wasteDate(LocalDateTime.now())
                    .wasteReason("유통기한 만료")
                    .store(inventory.getStore())
                    .product(inventory.getProduct())
                    .build();
            
            // 재고 차감
            inventory.setCount(0);
            inventory.setStatus(InventoryStatus.CRITICAL); // 폐기됨
            storeInventoryRepository.save(inventory);

            // POS 재고도 차감 (동기화)
            posStoreInventoryRepository.findByStore_IdxAndProduct_IdxAndManufacturedDate(
                inventory.getStore().getIdx(), inventory.getProduct().getIdx(), inventory.getManufacturedDate()
            ).ifPresent(posInventory -> {
                posInventory.setCount(0);
                posInventory.setStatus(InventoryStatus.CRITICAL);
                posStoreInventoryRepository.save(posInventory);
            });

            wasteLogRepository.save(entity);
            resList.add(WasteLogDto.RegRes.from(entity));
        }

        return resList;
    }



    @Transactional
    public WasteLogDto.WasteRes createWaste(Long userIdx, WasteLogDto.PosWasteReq req) {
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        PosStoreInventory posLot = posStoreInventoryRepository.findById(req.getPosStoreInventoryIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POS_INVENTORY_NOT_FOUND));

        if (!posLot.getStore().getIdx().equals(store.getIdx())) {
            throw new BaseException(BaseResponseStatus.STORE_INVENTORY_NOT_AUTHORIZED);
        }

        if (posLot.getCount() < req.getQuantity()) {
            throw new BaseException(BaseResponseStatus.WASTE_QUANTITY_EXCEEDS_STOCK);
        }

        StoreInventory hqLot = storeInventoryRepository
                .findByStore_IdxAndProduct_IdxAndManufacturedDate(
                        store.getIdx(), posLot.getProduct().getIdx(), posLot.getManufacturedDate())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_INVENTORY_NOT_FOUND));

        posLot.setCount(posLot.getCount() - req.getQuantity());
        hqLot.setCount(hqLot.getCount() - req.getQuantity());

        posLot.setStatus(resolveStatus(posLot.getCount(), posLot.getProduct().getMinStock()));
        hqLot.setStatus(resolveStatus(hqLot.getCount(), hqLot.getProduct().getMinStock()));

        long amountLoss = (long) req.getQuantity() * posLot.getProduct().getUnitPrice();

        WasteLog wasteLog = new WasteLog();
        wasteLog.setQuantity(req.getQuantity());
        wasteLog.setAmountLoss((int) amountLoss);
        wasteLog.setWasteDate(LocalDateTime.now());
        wasteLog.setWasteReason(req.getWasteReason());
        wasteLog.setStore(store);
        wasteLog.setProduct(posLot.getProduct());

        return WasteLogDto.WasteRes.from(wasteLogRepository.save(wasteLog));
    }

    private InventoryStatus resolveStatus(int count, int minStock) {
        if (count <= 0) return InventoryStatus.CRITICAL;
        // TODO: 유통기한 임박 상태 변경 로직 수정 필요
        if (count <= minStock / 2) return InventoryStatus.CRITICAL;
        if (count <= minStock) return InventoryStatus.LOW;

        return InventoryStatus.NORMAL;
    }

    public Long getInputMonthWasteSum(YearMonth inputYearMonth) {

        LocalDateTime startDateTime = inputYearMonth.atDay(1).atStartOfDay();

        LocalDateTime endDateTime = inputYearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        List<WasteLog> logs = wasteLogRepository.findAllByWasteDateBetween(startDateTime, endDateTime);
        Long wasteSum = 0L;
        for (WasteLog log : logs) {
            wasteSum += log.getQuantity();
        }
        return wasteSum;
    }

    public Long getInputMonthWasteAmountSum(YearMonth inputYearMonth) {
        LocalDateTime startDateTime = inputYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = inputYearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        List<WasteLog> logs = wasteLogRepository.findAllByWasteDateBetween(startDateTime, endDateTime);
        Long amountSum = 0L;
        for (WasteLog log : logs) {
            amountSum += log.getAmountLoss();
        }
        return amountSum;
    }

}
