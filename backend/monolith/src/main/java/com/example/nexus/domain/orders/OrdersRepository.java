package com.example.nexus.domain.orders;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.domain.orders.model.Orders;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {

    // Specification 기반 목록 조회 시 Store를 한 번에 JOIN FETCH (N+1 방지)
    @Override
    @EntityGraph(attributePaths = {"store"})
    Page<Orders> findAll(Specification<Orders> spec, Pageable pageable);

    // 정산용 - 매장별 전체 주문 목록 조회
    List<Orders> findAllByStoreIdx(Long storeIdx);

    // 본사 대시보드 - 발주 유형별 최근 건수 (자동/수동 KPI)
    long countByOrdersTypeAndCreatedAtAfter(OrdersType ordersType, LocalDateTime since);

    // 본사 대시보드 - 상태별 발주 건수 (확정 대기 KPI)
    long countByOrdersStatus(OrdersStatus ordersStatus);

    // 본사 대시보드 - 이상 발주 건수 KPI
    long countByIsDangerTrue();

    // 본사 대시보드 - 주간 발주 추이 차트 (일별 건수)
    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%m-%d'), COUNT(o) " +
            "FROM Orders o WHERE o.createdAt >= :since " +
            "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', o.createdAt, '%m-%d')")
    List<Object[]> findWeeklyOrderStats(@Param("since") LocalDateTime since);

    // 본사 대시보드 - 이상 발주 월별 추이 차트 (상태별 그룹핑)
    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m'), o.ordersStatus, COUNT(o) " +
            "FROM Orders o WHERE o.isDanger = true AND o.createdAt >= :since " +
            "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m'), o.ordersStatus " +
            "ORDER BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m')")
    List<Object[]> findDangerStatsByMonth(@Param("since") LocalDateTime since);

    // 동시성 제어 - 단건 비관적 잠금 (상태 변경 전용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orders o WHERE o.idx = :idx")
    Optional<Orders> findByIdForUpdate(@Param("idx") Long idx);

    // 동시성 제어 - 단건 비관적 잠금 + 연관 엔티티 로딩 (승인 시 출고 처리 필요)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.store " +
            "LEFT JOIN FETCH o.ordersItemList i " +
            "LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.delivery " +
            "WHERE o.idx = :idx")
    Optional<Orders> findByIdWithDetailsForUpdate(@Param("idx") Long idx);

    // 본사 발주 관리 - 일괄 승인 시 Store, OrdersItemList, Product 한 번에 로딩 + 비관적 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.store " +
            "JOIN FETCH o.ordersItemList i " +
            "JOIN FETCH i.product " +
            "WHERE o.ordersStatus = :status")
    List<Orders> findAllByOrdersStatusWithDetails(@Param("status") OrdersStatus status);

    // 본사 발주 관리 - 이상 발주 재평가 시 Store, OrdersItemList 한 번에 로딩 (N+1 방지)
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.store " +
            "LEFT JOIN FETCH o.ordersItemList " +
            "WHERE o.isDanger = true")
    List<Orders> findAllDangerWithDetails();

    // 본사 발주 관리 - 이상 발주 판별을 위한 매장별 평균 발주 수량 계산 (단건)
    @Query("SELECT COALESCE(SUM(i.count), 0) / GREATEST(COUNT(DISTINCT o.idx), 1) " +
            "FROM Orders o JOIN o.ordersItemList i " +
            "WHERE o.store.idx = :storeIdx AND o.createdAt >= :since AND (:excludeIdx IS NULL OR o.idx <> :excludeIdx)")
    Integer findAvgQtyByStoreAndPeriod(@Param("storeIdx") Long storeIdx, @Param("since") LocalDateTime since, @Param("excludeIdx") Long excludeIdx);

    // 본사 발주 관리 - 이상 발주 목록 조회 시 매장별 평균 수량 + 주문 총수량 일괄 계산 (N+1 방지)
    // 반환: [orders_idx, avgQty, totalQty]
    @Query(value = "SELECT o1.orders_idx, " +
            "CAST(COALESCE(SUM(oi_hist.count), 0) / GREATEST(COUNT(DISTINCT o2.orders_idx), 1) AS SIGNED), " +
            "CAST(COALESCE(cur.total_qty, 0) AS SIGNED) " +
            "FROM orders o1 " +
            "LEFT JOIN orders o2 ON o2.store_idx = o1.store_idx " +
            "  AND o2.created_at >= DATE_SUB(o1.created_at, INTERVAL :period MONTH) " +
            "  AND o2.orders_idx != o1.orders_idx " +
            "LEFT JOIN orders_item oi_hist ON oi_hist.orders_idx = o2.orders_idx " +
            "LEFT JOIN (SELECT orders_idx, SUM(count) AS total_qty FROM orders_item GROUP BY orders_idx) cur " +
            "  ON cur.orders_idx = o1.orders_idx " +
            "WHERE o1.orders_idx IN :orderIds " +
            "GROUP BY o1.orders_idx, cur.total_qty", nativeQuery = true)
    List<Object[]> findAvgQtyBatch(@Param("orderIds") List<Long> orderIds, @Param("period") int period);

    // 점주 대시보드 - 미확정 제안 발주서 개수 KPI
    long countByStore_IdxAndOrdersStatusAndOrdersType(Long storeIdx, OrdersStatus ordersStatus, OrdersType ordersType);

    // 점주 대시보드 - 기간별 승인 발주 금액 합계 (매출 KPI)
    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Orders o WHERE o.store.idx = :storeIdx AND o.ordersStatus = 'APPROVE' AND o.createdAt >= :from AND o.createdAt < :to")
    long sumApprovedPriceByStoreAndPeriod(@Param("storeIdx") Long storeIdx, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 점주 제안 발주서 & 점주 대시보드  - 미확정 제안 발주서 목록
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.ordersItemList i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE o.store.idx = :storeIdx AND o.ordersStatus = :ordersStatus AND o.ordersType = :ordersType " +
            "ORDER BY o.createdAt DESC")
    List<Orders> findAllByStore_IdxAndOrdersStatusAndOrdersTypeOrderByCreatedAtDesc(
            @Param("storeIdx") Long storeIdx,
            @Param("ordersStatus") OrdersStatus ordersStatus,
            @Param("ordersType") OrdersType ordersType);

    // 점주 제안 발주서 - Store, OrdersItemList, Product 한 번에 로딩 (N+1 방지)
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.store " +
            "JOIN FETCH o.ordersItemList i " +
            "JOIN FETCH i.product " +
            "WHERE o.store.idx = :storeIdx AND o.ordersStatus = :status AND o.ordersType = :type " +
            "ORDER BY o.createdAt DESC")
    List<Orders> findPendingWithDetails(@Param("storeIdx") Long storeIdx, @Param("status") OrdersStatus status, @Param("type") OrdersType type);

    // 발주 상세 조회 - Store, OrdersItemList, Product 한 번에 로딩 (N+1 방지)
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.store " +
            "LEFT JOIN FETCH o.ordersItemList i " +
            "LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.delivery " +
            "WHERE o.idx = :idx")
    Optional<Orders> findByIdWithDetails(@Param("idx") Long idx);

    // 점주 발주 관리 - 점주 이력 페이징 조회
    @EntityGraph(attributePaths = {"store", "delivery"})
    Page<Orders> findAllByStore_IdxAndOrdersStatusIn(Long storeIdx, List<OrdersStatus> ordersStatuses, Pageable pageable);
}
