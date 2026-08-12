package com.example.nexus.domain.wastelog;

import com.example.nexus.domain.wastelog.model.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WasteLogRepository extends JpaRepository<WasteLog, Long> {
    List<WasteLog> findAllByWasteDateBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT FUNCTION('DATE_FORMAT', w.wasteDate, '%Y-%m'), SUM(w.quantity) " +
           "FROM WasteLog w " +
           "WHERE w.wasteDate >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', w.wasteDate, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', w.wasteDate, '%Y-%m')")
    List<Object[]> findMonthlyWasteTrend(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT c.categoryName, SUM(w.quantity) " +
           "FROM WasteLog w " +
           "JOIN w.product p " +
           "JOIN p.category c " +
           "WHERE w.wasteDate BETWEEN :start AND :end " +
           "GROUP BY c.categoryName")
    List<Object[]> findCategoryWasteRatio(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.storeName, " +
           "COUNT(w), " +
           "SUM(CASE WHEN w.wasteReason != '유통기한 만료' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN w.wasteReason = '유통기한 만료' THEN 1 ELSE 0 END) " +
           "FROM WasteLog w " +
           "JOIN w.store s " +
           "WHERE w.wasteDate BETWEEN :start AND :end " +
           "GROUP BY s.storeName")
    List<Object[]> findStoreWasteStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
