package com.example.nexus.domain.orders;

import com.example.nexus.domain.orders.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrdersSettlementRepository extends JpaRepository<Orders, Long> {
    // 해당 월 전체 발주 금액 합계 (총 납부액)
    @Query("""
            SELECT COALESCE(SUM(o.price), 0)
            FROM Orders o
            WHERE o.store.idx = :storeIdx
              AND o.createdAt >= :start
              AND o.createdAt < :end
            """)
    Long sumTotalAmountByMonth(
            @Param("storeIdx") Long storeIdx,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 해당 월 APPROVE 상태 발주 금액 합계 (납부 예상 금액)
    @Query("""
            SELECT COALESCE(SUM(o.price), 0)
            FROM Orders o
            WHERE o.store.idx = :storeIdx
              AND o.createdAt >= :start
              AND o.createdAt < :end
              AND o.ordersStatus = 'APPROVE'
            """)
    Long sumExpectedAmountByMonth(
            @Param("storeIdx") Long storeIdx,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 해당 월 발주 내역 페이징 조회 (최신순)
    @Query("""
            SELECT o
            FROM Orders o
            WHERE o.store.idx = :storeIdx
              AND o.createdAt >= :start
              AND o.createdAt < :end
            ORDER BY o.createdAt DESC
            """)
    Page<Orders> findOrderHistoryByMonth(
            @Param("storeIdx") Long storeIdx,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
