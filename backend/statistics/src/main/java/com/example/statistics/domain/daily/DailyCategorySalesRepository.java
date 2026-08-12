package com.example.statistics.domain.daily;

import com.example.statistics.domain.daily.model.DailyCategorySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 카테고리별 매출 영구 저장소.
 * Redis 키 `sales:category:DATE` (Hash) 의 카테고리별 집계를 MariaDB 에 dump 한 결과를 관리.
 * 한 날짜에 판매된 카테고리 수만큼 row 가 존재 (aggregate_date + category_name 복합 UNIQUE).
 */
public interface DailyCategorySalesRepository extends JpaRepository<DailyCategorySales, Long> {

    /**
     * 특정 날짜의 카테고리별 매출을 조회.
     * 판매된 카테고리 수만큼 row 가 반환되므로 List 형태.
     *
     * @param aggregateDate 조회할 집계 날짜
     * @return 해당 날짜의 카테고리별 매출 목록 (없으면 빈 List)
     */
    List<DailyCategorySales> findByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 날짜의 dump 가 이미 수행되었는지 확인.
     * 스케줄러/수동 트리거 의 멱등성 체크에 사용.
     *
     * @param aggregateDate 확인할 집계 날짜
     * @return 이미 dump 되었으면 true, 아니면 false
     */
    boolean existsByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 기간(start ~ end) 내 카테고리별 매출 합계 (장기 통계).
     * 카테고리별로 합산해서 매출 큰 순으로 정렬.
     *
     * @param start 시작 날짜 (포함)
     * @param end   종료 날짜 (포함)
     * @return [categoryName(String), amount(Long)] 배열 리스트
     */
    @Query(value = """
            SELECT category_name, SUM(amount) AS amount
            FROM daily_category_sales
            WHERE aggregate_date BETWEEN :start AND :end
            GROUP BY category_name
            ORDER BY amount DESC
            """, nativeQuery = true)
    List<Object[]> findCategorySalesGroupByRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
