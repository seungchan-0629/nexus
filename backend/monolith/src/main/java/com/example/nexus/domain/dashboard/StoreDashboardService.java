package com.example.nexus.domain.dashboard;

import com.example.nexus.common.enums.DeliveryStatus;
import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.dashboard.model.StoreDashboardDto;
import com.example.nexus.domain.delivery.DeliveryRepository;
import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.orders.OrdersRepository;
import com.example.nexus.domain.pos.PosPayRepository;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreDashboardService {
    private final PosPayRepository posPayRepository;
    private final OrdersRepository ordersRepository;
    private final DeliveryRepository deliveryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final StoreRepository storeRepository;

    /**
     * 금일 매출 KPI 조회
     * - 로그인된 점주의 매장에서 당일 발생한 POS 매출 총액을 집계
     * - 전일 매출과 비교하여 증감률(%) 계산
     */
    public StoreDashboardDto.SalesKpiRes getSalesKpi(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 금일/전일 기간 설정
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        // POS 매출 합산
        long todaySales = posPayRepository.sumPayAmountByStoreAndPeriod(store.getIdx(), todayStart, todayEnd);
        long yesterdaySales = posPayRepository.sumPayAmountByStoreAndPeriod(store.getIdx(), yesterdayStart, todayStart);

        // 전일 대비 증감률 계산 (전일 매출이 0이면 0.0%)
        double deltaPercent = 0.0;
        if (yesterdaySales > 0) {
            deltaPercent = Math.round((todaySales - yesterdaySales) * 1000.0 / yesterdaySales) / 10.0;
        }

        return StoreDashboardDto.SalesKpiRes.builder()
                .todaySales(todaySales)
                .deltaPercent(deltaPercent)
                .build();
    }

    /**
     * 제안 발주서 KPI 조회
     * - 점주 매장에 대해 본사가 자동 생성한 발주서 중 승인 대기(WAITING) 상태 건수 반환
     */
    public StoreDashboardDto.PendingOrderKpiRes getPendingOrderKpi(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 승인 대기 상태의 자동 발주 건수 조회
        long pendingCount = ordersRepository.countByStore_IdxAndOrdersStatusAndOrdersType(
                store.getIdx(), OrdersStatus.WAITING, OrdersType.AUTO);

        return StoreDashboardDto.PendingOrderKpiRes.builder()
                .pendingCount(pendingCount)
                .build();
    }

    /**
     * 재고 위험 품목 KPI 조회
     * - 점주 매장의 재고 중 LOW(주의)/CRITICAL(위험) 상태인 품목 수 반환
     */
    public StoreDashboardDto.InventoryRiskKpiRes getInventoryRiskKpi(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 상태별 위험 재고 건수 조회
        long lowCount = storeInventoryRepository.countByStore_IdxAndStatus(store.getIdx(), InventoryStatus.LOW);
        long criticalCount = storeInventoryRepository.countByStore_IdxAndStatus(store.getIdx(), InventoryStatus.CRITICAL);

        return StoreDashboardDto.InventoryRiskKpiRes.builder()
                .lowCount(lowCount)
                .criticalCount(criticalCount)
                .totalDangerCount(lowCount + criticalCount)
                .build();
    }

    /**
     * 정산 현황 KPI 조회
     * - 반월 단위(1~15일, 16~말일) 구간의 APPROVE 상태 발주 금액 합산
     * - 직전 구간 대비 증감률(%) 계산
     */
    public StoreDashboardDto.SettlementKpiRes getSettlementKpi(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 현재/직전 반월 구간 계산
        LocalDate today = LocalDate.now();
        LocalDateTime currentStart;
        LocalDateTime currentEnd;
        LocalDateTime prevStart;
        LocalDateTime prevEnd;
        String currentPeriod;

        if (today.getDayOfMonth() <= 15) {
            // 현재 구간: 이번 달 1~15일
            currentStart = today.withDayOfMonth(1).atStartOfDay();
            currentEnd = today.withDayOfMonth(16).atStartOfDay();
            // 직전 구간: 지난 달 16~말일
            LocalDate prevMonth = today.minusMonths(1);
            prevStart = prevMonth.withDayOfMonth(16).atStartOfDay();
            prevEnd = prevMonth.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            currentPeriod = today.getYear() + "-" + String.format("%02d", today.getMonthValue()) + " 상반기";
        } else {
            // 현재 구간: 이번 달 16~말일
            currentStart = today.withDayOfMonth(16).atStartOfDay();
            currentEnd = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            // 직전 구간: 이번 달 1~15일
            prevStart = today.withDayOfMonth(1).atStartOfDay();
            prevEnd = today.withDayOfMonth(16).atStartOfDay();
            currentPeriod = today.getYear() + "-" + String.format("%02d", today.getMonthValue()) + " 하반기";
        }

        // APPROVE 상태 발주 금액 합산
        long currentAmount = ordersRepository.sumApprovedPriceByStoreAndPeriod(store.getIdx(), currentStart, currentEnd);
        long prevAmount = ordersRepository.sumApprovedPriceByStoreAndPeriod(store.getIdx(), prevStart, prevEnd);

        // 직전 구간 대비 증감률 계산
        double deltaPercent = 0.0;
        if (prevAmount > 0) {
            deltaPercent = Math.round((currentAmount - prevAmount) * 1000.0 / prevAmount) / 10.0;
        }

        return StoreDashboardDto.SettlementKpiRes.builder()
                .currentAmount(currentAmount)
                .currentPeriod(currentPeriod)
                .deltaPercent(deltaPercent)
                .build();
    }

    /**
     * 일별 매출 추이 차트 조회
     * - 이번 주(일~토)와 지난 주의 일별 POS 매출을 비교
     * - 단위: 원 (프론트에서 만원 변환)
     */
    public StoreDashboardDto.DailySalesChartRes getDailySalesChart(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 이번 주 일요일 ~ 토요일 구간 계산
        LocalDate today = LocalDate.now();
        LocalDate thisWeekSunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate lastWeekSunday = thisWeekSunday.minusWeeks(1);

        LocalDateTime thisWeekStart = thisWeekSunday.atStartOfDay();
        LocalDateTime thisWeekEnd = thisWeekSunday.plusWeeks(1).atStartOfDay();
        LocalDateTime lastWeekStart = lastWeekSunday.atStartOfDay();
        LocalDateTime lastWeekEnd = thisWeekStart;

        // 일별 매출 조회
        Map<LocalDate, Long> thisWeekMap = toDailySalesMap(
                posPayRepository.findDailySalesByStoreAndPeriod(store.getIdx(), thisWeekStart, thisWeekEnd));
        Map<LocalDate, Long> lastWeekMap = toDailySalesMap(
                posPayRepository.findDailySalesByStoreAndPeriod(store.getIdx(), lastWeekStart, lastWeekEnd));

        // 요일별 데이터 배열 생성 (일~토)
        List<String> labels = Arrays.asList("일", "월", "화", "수", "목", "금", "토");
        Long[] thisWeekData = new Long[7];
        Long[] lastWeekData = new Long[7];

        for (int i = 0; i < 7; i++) {
            thisWeekData[i] = thisWeekMap.getOrDefault(thisWeekSunday.plusDays(i), 0L);
            lastWeekData[i] = lastWeekMap.getOrDefault(lastWeekSunday.plusDays(i), 0L);
        }

        return StoreDashboardDto.DailySalesChartRes.builder()
                .labels(labels)
                .thisWeek(Arrays.asList(thisWeekData))
                .lastWeek(Arrays.asList(lastWeekData))
                .build();
    }

    /**
     * 제안 발주서 목록 조회
     * - 점주 매장에 대해 본사가 자동 생성한 발주서 중 승인 대기(WAITING) 상태 목록 반환
     */
    public List<StoreDashboardDto.PendingOrderItem> getPendingOrderList(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // WAITING + AUTO 발주서 목록 조회
        return ordersRepository
                .findAllByStore_IdxAndOrdersStatusAndOrdersTypeOrderByCreatedAtDesc(store.getIdx(), OrdersStatus.WAITING, OrdersType.AUTO)
                .stream().map(StoreDashboardDto.PendingOrderItem::from).toList();
    }

    /**
     * 나의 배송 현황 목록 조회
     * - 점주 매장으로 배송 중인 건의 상태 목록 반환 (배송완료 제외)
     */
    public List<StoreDashboardDto.MyDeliveryItem> getMyDeliveryList(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 배송완료(DELIVERED) 제외한 배송 목록 조회
        return deliveryRepository
                .findByOrders_Store_IdxAndDeliveryStatusNotOrderByDepartureDateDesc(store.getIdx(), DeliveryStatus.DELIVERED)
                .stream().map(StoreDashboardDto.MyDeliveryItem::from).toList();
    }

    /**
     * 재고 위험 품목 목록 조회
     * - 점주 매장의 LOW/CRITICAL 상태 재고를 품목명, 현재 재고, 최소 재고, 상태와 함께 반환
     */
    public List<StoreDashboardDto.InventoryWarningItem> getInventoryWarningList(Long userIdx) {
        // 점주의 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // LOW/CRITICAL 상태 재고 목록 조회 (CRITICAL 우선 정렬)
        return storeInventoryRepository
                .findByStore_IdxAndStatusInOrderByStatusDesc(store.getIdx(), List.of(InventoryStatus.CRITICAL, InventoryStatus.LOW))
                .stream().map(StoreDashboardDto.InventoryWarningItem::from).toList();
    }

    private Map<LocalDate, Long> toDailySalesMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new java.util.HashMap<>();
        for (Object[] row : rows) {
            LocalDate date;
            if (row[0] instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else {
                date = (LocalDate) row[0];
            }
            Long amount = ((Number) row[1]).longValue();
            map.put(date, amount);
        }
        return map;
    }
}
