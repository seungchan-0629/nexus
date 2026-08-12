package com.example.statistics.domain.daily;

import com.example.statistics.domain.daily.model.DailyMenuSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 메뉴별 판매량 영구 저장소.
 * Redis 키 `sales:menu:ranking:DATE` (ZSet) 의 메뉴별 집계를 MariaDB 에 dump 한 결과를 관리.
 * 한 날짜에 판매된 메뉴 수만큼 row 가 존재 (aggregate_date + menu_idx 복합 UNIQUE).
 */
public interface DailyMenuSalesRepository extends JpaRepository<DailyMenuSales, Long> {

    /**
     * 특정 날짜의 모든 메뉴 판매량을 조회.
     */
    List<DailyMenuSales> findByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 날짜의 dump 가 이미 수행되었는지 확인 (멱등성 체크용).
     */
    boolean existsByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 기간(start ~ end) 내 메뉴별 판매량 합계 (장기 통계).
     * 메뉴별로 합산해서 판매량 많은 순으로 정렬.
     *
     * @param start 시작 날짜 (포함)
     * @param end   종료 날짜 (포함)
     * @return [menuIdx(Long), menuName(String), totalQuantity(Long)] 배열 리스트
     */
    @Query(value = """
            SELECT menu_idx, menu_name, SUM(quantity) AS total_qty
            FROM daily_menu_sales
            WHERE aggregate_date BETWEEN :start AND :end
            GROUP BY menu_idx, menu_name
            ORDER BY total_qty DESC
            """, nativeQuery = true)
    List<Object[]> findMenuSalesGroupByRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
