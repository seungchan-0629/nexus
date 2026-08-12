package com.example.nexus.domain.dashboard;

import com.example.nexus.common.enums.DeliveryStatus;
import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.domain.dashboard.model.DashboardDto;
import com.example.nexus.domain.delivery.DeliveryRepository;
import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.head.HeadInventoryRepository;
import com.example.nexus.domain.head.model.HeadInventory;
import com.example.nexus.domain.orders.OrdersRepository;
import com.example.nexus.domain.store.StoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final StoreRepository storeRepository;
    private final OrdersRepository ordersRepository;
    private final HeadInventoryRepository headInventoryRepository;
    private final DeliveryRepository deliveryRepository;
    // 본사 대시보드 입점 매장 카드 데이터 조회 메서드
    public DashboardDto.StoreKpiRes getStoreKpi() {

        // 총 매장 수
        long totalCount = storeRepository.countByIsDeletedFalse();

        // 이번 달 1일 00:00 기준 신규 매장 수
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long newThisMonth = storeRepository.countByIsDeletedFalseAndCreatedAtAfter(monthStart);

        // 전일 대비 증감률: 어제까지 총 매장 수 vs 오늘까지 총 매장 수
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long yesterdayTotal = storeRepository.countByIsDeletedFalseAndCreatedAtBefore(todayStart);
        double delta = yesterdayTotal > 0 ? (double) (totalCount - yesterdayTotal) * 100 / yesterdayTotal : 0;

        return DashboardDto.StoreKpiRes.builder()
                .totalStoreCount(totalCount)
                .newStoreCountThisMonth(newThisMonth)
                .deltaPercent(Math.round(delta * 10) / 10.0)
                .build();
    }

    // 본사 대시보드 발주 카드 데이터 조회 메서드
    public DashboardDto.OrdersKpiRes getOrdersKpi() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 금일 자동 발주 건수
        long todayAutoCount = ordersRepository.countByOrdersTypeAndCreatedAtAfter(OrdersType.AUTO, todayStart);

        // 현재 확정 상태 발주 건수
        long confirmedCount = ordersRepository.countByOrdersStatus(OrdersStatus.CONFIRMED);

        // 금일 수동 발주 건수
        long todayManualCount = ordersRepository.countByOrdersTypeAndCreatedAtAfter(OrdersType.MANUAL, todayStart);

        // 이상 발주 건수
        long dangerCount = ordersRepository.countByIsDangerTrue();

        return DashboardDto.OrdersKpiRes.builder()
                .todayAutoCount(todayAutoCount)
                .confirmedCount(confirmedCount)
                .todayManualCount(todayManualCount)
                .dangerCount(dangerCount)
                .build();
    }

    // 본사 재고 위험 카드 데이터 조회 메서드
    public DashboardDto.InventoryKpiRes getInventoryKpi() {

        // 재고 부족 건수
        long lowCount = headInventoryRepository.countByStatus(InventoryStatus.LOW);

        // 재고 위험 건수
        long criticalCount = headInventoryRepository.countByStatus(InventoryStatus.CRITICAL);

        return DashboardDto.InventoryKpiRes.builder()
                .lowCount(lowCount)
                .criticalCount(criticalCount)
                .totalDangerCount(lowCount + criticalCount)
                .build();
    }

    // 본사 대시보드 배송 현황 카드 데이터 조회 메서드
    public DashboardDto.DeliveryKpiRes getDeliveryKpi() {
        List<DeliveryStatus> ongoingStatuses = List.of(DeliveryStatus.START, DeliveryStatus.DELIVERYING);

        // 진행중 배송 건수
        long ongoingCount = deliveryRepository.countByDeliveryStatusIn(ongoingStatuses);

        // 배송 지연 건수
        long delayCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.DELAY);

        return DashboardDto.DeliveryKpiRes.builder()
                .ongoingCount(ongoingCount)
                .delayCount(delayCount)
                .build();
    }

    // 본사 대시보드 주간 발주 통계 데이터 조회 메서드
    public DashboardDto.WeeklyOrderStatsRes getWeeklyOrderStats() {
        // 이번 주 월요일 00:00 기준으로 이번 주 / 지난 주 범위 계산
        // 예: 오늘 2026-05-04(일) → 이번 주 월요일 04-28, 지난 주 월요일 04-21
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        LocalDateTime since = lastMonday.atStartOfDay();

        // 요일 라벨 (월~일)
        List<String> labels = List.of("월", "화", "수", "목", "금", "토", "일");

        // 지난 주, 이번 주 각각 7일치 카운트 초기화
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, Long> lastWeekMap = new LinkedHashMap<>();
        Map<String, Long> thisWeekMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            lastWeekMap.put(lastMonday.plusDays(i).format(formatter), 0L);
            thisWeekMap.put(thisMonday.plusDays(i).format(formatter), 0L);
        }

        // DB에서 2주치 일별 발주 건수 조회 후 각 주에 매핑
        List<Object[]> rows = ordersRepository.findWeeklyOrderStats(since);
        for (Object[] row : rows) {
            String day = (String) row[0];
            long count = (Long) row[1];

            if (lastWeekMap.containsKey(day)) {
                lastWeekMap.put(day, count);
            } else if (thisWeekMap.containsKey(day)) {
                thisWeekMap.put(day, count);
            }
        }

        return DashboardDto.WeeklyOrderStatsRes.builder()
                .labels(labels)
                .thisWeek(new ArrayList<>(thisWeekMap.values()))
                .lastWeek(new ArrayList<>(lastWeekMap.values()))
                .build();
    }

    // 본사 대시보드 재고 위험 리스트 조회 메서드
    public DashboardDto.DangerInventoryRes getDangerInventoryList(int page, int size) {
        // 위험 재고: status가 LOW 또는 CRITICAL인 본사 재고 목록
        List<InventoryStatus> dangerStatuses = List.of(InventoryStatus.LOW, InventoryStatus.CRITICAL);

        Slice<HeadInventory> slice = headInventoryRepository.findByStatusIn(dangerStatuses, PageRequest.of(page, size));

        List<DashboardDto.DangerInventoryItem> items = slice.getContent().stream().map(DashboardDto.DangerInventoryItem::from).toList();

        return DashboardDto.DangerInventoryRes.builder()
                .items(items)
                .hasNext(slice.hasNext())
                .build();
    }

    // 본사 대시보드 이상 발주 통계 데이터 조회
    public DashboardDto.DangerStatsRes getOrdersDangerStats() {
        // 조회 시작 시점: 현재 월 포함 6개월 전 1일 00:00
        // 예: 현재 2026-05 → 2025-12-01 00:00부터 조회
        LocalDateTime since = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay();

        // 6개월치 라벨(yyyy-MM)을 순서대로 생성하고, 각 월별 카운트 배열 초기화
        // LinkedHashMap으로 삽입 순서 보장 → 프론트 차트 x축 순서 유지
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, long[]> monthMap = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            String label = YearMonth.now().minusMonths(5 - i).format(formatter);
            monthMap.put(label, new long[3]); // [0: 승인대기(CONFIRMED), 1: 승인(APPROVE), 2: 반려(REJECT)]
        }

        // DB에서 월별 + 상태별 이상 발주 건수 조회 후 monthMap에 매핑
        List<Object[]> rows = ordersRepository.findDangerStatsByMonth(since);

        for (Object[] row : rows) {
            String month = (String) row[0];
            long count = (Long) row[2];

            long[] counts = monthMap.get(month);
            if (counts == null) {
                continue;
            }

            OrdersStatus status = (OrdersStatus) row[1];
            switch (status) {
                case CONFIRMED :
                    counts[0] = count;
                    break;
                case APPROVE :
                    counts[1] = count;
                    break;
                case REJECT :
                    counts[2] = count;
                    break;
                default: break;
            }
        }

        // monthMap을 프론트 차트에 맞는 배열 형태로 변환
        List<String> labels = new ArrayList<>(monthMap.keySet());
        List<Long> confirmed = new ArrayList<>();
        List<Long> approved = new ArrayList<>();
        List<Long> rejected = new ArrayList<>();

        for (long[] counts : monthMap.values()) {
            confirmed.add(counts[0]);
            approved.add(counts[1]);
            rejected.add(counts[2]);
        }

        return DashboardDto.DangerStatsRes.builder()
                .labels(labels)
                .confirmed(confirmed)
                .approved(approved)
                .rejected(rejected)
                .build();
    }

    // 본사 대시보드 지연 배송 목록 데이터 조회 메서드
    public DashboardDto.DelayDeliveryRes getDelayDeliveryList(int page, int size) {
        // 지연 배송 목록: status가 DELAY인 배송
        Slice<Delivery> slice = deliveryRepository.findByDeliveryStatus(DeliveryStatus.DELAY, PageRequest.of(page, size));

        List<DashboardDto.DeliveryItem> items = slice.getContent().stream().map(DashboardDto.DeliveryItem::from).toList();

        return DashboardDto.DelayDeliveryRes.builder()
                .items(items)
                .hasNext(slice.hasNext())
                .build();
    }

    // 본사 대시보드 배송 비율 데이터 조회 메서드
    public DashboardDto.DeliveryRatioRes getDeliveryRatio() {
        // 최근 한달 기준 배송 상태별 건수 조회
        LocalDateTime monthAgo = LocalDate.now().minusMonths(1).atStartOfDay();
        long ready = deliveryRepository.countByDeliveryStatusAndDepartureDateAfter(DeliveryStatus.READY, monthAgo);
        long start = deliveryRepository.countByDeliveryStatusAndDepartureDateAfter(DeliveryStatus.START, monthAgo);
        long delivering = deliveryRepository.countByDeliveryStatusAndDepartureDateAfter(DeliveryStatus.DELIVERYING, monthAgo);
        long delivered = deliveryRepository.countByDeliveryStatusAndDepartureDateAfter(DeliveryStatus.DELIVERED, monthAgo);
        long delay = deliveryRepository.countByDeliveryStatusAndDepartureDateAfter(DeliveryStatus.DELAY, monthAgo);

        return DashboardDto.DeliveryRatioRes.builder()
                .ready(ready)
                .start(start)
                .delivering(delivering)
                .delivered(delivered)
                .delay(delay)
                .build();
    }
}
