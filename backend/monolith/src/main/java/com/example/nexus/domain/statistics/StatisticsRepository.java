package com.example.nexus.domain.statistics;

import com.example.nexus.domain.pos.model.PosPay;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<PosPay, Long> {

    // 오늘 총 매출
    @Query("SELECT COALESCE(SUM(p.payAmount), 0) FROM PosPay p WHERE p.paidAt >= :start AND p.paidAt < :end")
    long sumTodaySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 매장별 매출 랭킹
    @Query("SELECT p.store.idx, p.store.storeName, SUM(p.payAmount) " +
            "FROM PosPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY p.store.idx, p.store.storeName " +
            "ORDER BY SUM(p.payAmount) DESC")
    List<Object[]> findTopStores(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // 인기 메뉴 랭킹 (판매 수량 기준)
    @Query("SELECT m.idx, m.menuName, SUM(poi.quantity) " +
            "FROM PosOrdersItem poi " +
            "JOIN poi.menu m " +
            "JOIN poi.posPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY m.idx, m.menuName " +
            "ORDER BY SUM(poi.quantity) DESC")
    List<Object[]> findTopMenus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // 시간대별 매출 추이
    @Query("SELECT HOUR(p.paidAt), COALESCE(SUM(p.payAmount), 0) " +
            "FROM PosPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY HOUR(p.paidAt) " +
            "ORDER BY HOUR(p.paidAt)")
    List<Object[]> findHourlySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 카테고리별 매출 비율
    @Query("SELECT mc.menuCategoryName, COALESCE(SUM(poi.quantity * m.price), 0) " +
            "FROM PosOrdersItem poi " +
            "JOIN poi.menu m " +
            "JOIN m.menuCategory mc " +
            "JOIN poi.posPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY mc.menuCategoryName " +
            "ORDER BY SUM(poi.quantity * m.price) DESC")
    List<Object[]> findSalesByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 결제 수단별 매출 비율
    @Query("SELECT p.method, COALESCE(SUM(p.payAmount), 0) " +
            "FROM PosPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY p.method")
    List<Object[]> findSalesByPayMethod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
