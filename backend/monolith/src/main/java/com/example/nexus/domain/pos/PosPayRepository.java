package com.example.nexus.domain.pos;

import com.example.nexus.domain.pos.model.PosPay;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PosPayRepository extends JpaRepository<PosPay, Long> {
    List<PosPay> findByStoreIdxAndPaidAtBetweenOrderByPaidAtDesc(Long storeIdx, LocalDateTime from, LocalDateTime to);

    List<PosPay> findByStoreIdxAndPaidAtBetweenAndStoreInventoryDeductedAtIsNullOrderByPaidAtAsc(Long storeIdx, LocalDateTime from, LocalDateTime to);

    // 대시보드: 반개방 구간 [from, to)
    @Query("SELECT COALESCE(SUM(p.payAmount), 0) FROM PosPay p WHERE p.store.idx = :storeIdx AND p.paidAt >= :from AND p.paidAt < :to")
    long sumPayAmountByStoreAndPeriod(@Param("storeIdx") Long storeIdx, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 대시보드: 기간 내 일별 매출 합산 (날짜, 금액)
    @Query("SELECT CAST(p.paidAt AS LocalDate), COALESCE(SUM(p.payAmount), 0) FROM PosPay p WHERE p.store.idx = :storeIdx AND p.paidAt >= :from AND p.paidAt < :to GROUP BY CAST(p.paidAt AS LocalDate) ORDER BY CAST(p.paidAt AS LocalDate)")
    List<Object[]> findDailySalesByStoreAndPeriod(@Param("storeIdx") Long storeIdx, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // AI 보고서: 특정 기간 총 매출 합계
    @Query("SELECT COALESCE(SUM(p.payAmount), 0) FROM PosPay p WHERE p.paidAt BETWEEN :startDate AND :endDate")
    Long sumSalesByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 실시간 대시보드: 오늘 전체 매출 합계
    @Query("SELECT COALESCE(SUM(p.payAmount), 0) FROM PosPay p WHERE p.paidAt >= :start AND p.paidAt < :end")
    Long sumTodaySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 실시간 대시보드: 매장별 매출 랭킹 TOP N
    @Query("SELECT p.store.idx, p.store.storeName, SUM(p.payAmount) " +
            "FROM PosPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY p.store.idx, p.store.storeName " +
            "ORDER BY SUM(p.payAmount) DESC")
    List<Object[]> findTopStoresByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}