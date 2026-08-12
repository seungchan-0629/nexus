package com.example.nexus.domain.pos;

import com.example.nexus.domain.pos.model.PosOrdersItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PosOrdersItemRepository extends JpaRepository<PosOrdersItem, Long> {
    List<PosOrdersItem> findByPosPay_IdxIn(List<Long> payIdxList);

    // AI 보고서: N+1 문제 없는 인기 메뉴 통계 (수량 quantity 합산)
    @Query("SELECT m.menuName " +
            "FROM PosOrdersItem poi " +
            "JOIN poi.menu m " +
            "JOIN poi.posPay p " +
            "WHERE p.paidAt BETWEEN :startDate AND :endDate " +
            "GROUP BY m.menuName " +
            "ORDER BY SUM(poi.quantity) DESC")
    List<String> findTopSellingMenus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // 실시간 대시보드: 메뉴별 판매 수량 랭킹 TOP N
    @Query("SELECT m.idx, m.menuName, SUM(poi.quantity) " +
            "FROM PosOrdersItem poi " +
            "JOIN poi.menu m " +
            "JOIN poi.posPay p " +
            "WHERE p.paidAt >= :start AND p.paidAt < :end " +
            "GROUP BY m.idx, m.menuName " +
            "ORDER BY SUM(poi.quantity) DESC")
    List<Object[]> findTopMenusByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}