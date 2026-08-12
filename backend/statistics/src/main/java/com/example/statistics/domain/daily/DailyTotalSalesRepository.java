package com.example.statistics.domain.daily;

import com.example.statistics.domain.daily.model.DailyTotalSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일별 총 매출 영구 저장소.
 * Redis 키 `sales:today:DATE` 의 일별 집계를 MariaDB 에 dump 한 결과를 관리.
 * 한 날짜당 1 row 만 존재 (aggregate_date UNIQUE).
 */
public interface DailyTotalSalesRepository extends JpaRepository<DailyTotalSales, Long> {

    /**
     * 특정 날짜의 총 매출을 조회.
     *
     * @param aggregateDate 조회할 집계 날짜
     * @return 해당 날짜의 총 매출 (없으면 Optional.empty())
     */
    Optional<DailyTotalSales> findByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 날짜의 dump 가 이미 수행되었는지 확인.
     * 스케줄러/수동 트리거 의 멱등성 체크에 사용.
     *
     * @param aggregateDate 확인할 집계 날짜
     * @return 이미 dump 되었으면 true, 아니면 false
     */
    boolean existsByAggregateDate(LocalDate aggregateDate);

    /**
     * 연도별 매출 집계 (장기 통계).
     *
     * @return [year(Integer), total(Long)] 배열 리스트, 연도 오름차순
     */
    @Query(value = """
            SELECT YEAR(aggregate_date) AS year, SUM(total_amount) AS total
            FROM daily_total_sales
            GROUP BY YEAR(aggregate_date)
            ORDER BY YEAR(aggregate_date)
            """, nativeQuery = true)
    List<Object[]> findYearlySalesGroup();

    /**
     * 특정 연도의 월별 매출 집계 (장기 통계).
     *
     * @param year 조회할 연도
     * @return [month(Integer), total(Long)] 배열 리스트, 월 오름차순
     */
    @Query(value = """
            SELECT MONTH(aggregate_date) AS month, SUM(total_amount) AS total
            FROM daily_total_sales
            WHERE YEAR(aggregate_date) = :year
            GROUP BY MONTH(aggregate_date)
            ORDER BY MONTH(aggregate_date)
            """, nativeQuery = true)
    List<Object[]> findMonthlySalesGroup(@Param("year") int year);

    /**
     * 특정 연도의 분기별 매출 집계 (장기 통계).
     *
     * @param year 조회할 연도
     * @return [quarter(1~4), total(Long)] 배열 리스트, 분기 오름차순
     */
    @Query(value = """
            SELECT QUARTER(aggregate_date) AS quarter, SUM(total_amount) AS total
            FROM daily_total_sales
            WHERE YEAR(aggregate_date) = :year
            GROUP BY QUARTER(aggregate_date)
            ORDER BY QUARTER(aggregate_date)
            """, nativeQuery = true)
    List<Object[]> findQuarterlySalesGroup(@Param("year") int year);
}
