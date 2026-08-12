package com.example.nexus.domain.store;

import com.example.nexus.domain.pos.model.PosPay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StoreIncomeRepository extends JpaRepository<PosPay, Long> {


    @Query("""
            SELECT YEAR(p.paidAt), MONTH(p.paidAt), SUM(p.payAmount)
            FROM PosPay p
            WHERE p.store.idx = :storeIdx
            GROUP BY YEAR(p.paidAt), MONTH(p.paidAt)
            ORDER BY YEAR(p.paidAt) ASC, MONTH(p.paidAt) ASC
            """)
    List<Object[]> findMonthlyIncomeSumByStoreIdx(@Param("storeIdx") Long storeIdx);


    // 특정 월의 전체 매출 합계
    @Query("""
            SELECT COALESCE(SUM(p.payAmount), 0)
            FROM PosPay p
            WHERE p.store.idx = :storeIdx
              AND p.paidAt >= :start
              AND p.paidAt < :end
            """)
    Long sumTotalAmountByMonth(
            @Param("storeIdx") Long storeIdx,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 특정 월의 결제 내역 페이징 조회
    @Query("""
            SELECT p
            FROM PosPay p
            WHERE p.store.idx = :storeIdx
              AND p.paidAt >= :start
              AND p.paidAt < :end
            ORDER BY p.paidAt DESC
            """)
    Page<PosPay> findPayHistoryByMonth(
            @Param("storeIdx") Long storeIdx,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}