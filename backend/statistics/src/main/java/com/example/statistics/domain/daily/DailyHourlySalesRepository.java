package com.example.statistics.domain.daily;

import com.example.statistics.domain.daily.model.DailyHourlySales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 시간대별 매출 영구 저장소.
 * Redis 키 `sales:hourly:DATE` (Hash) 의 시간대별 집계를 MariaDB 에 dump 한 결과를 관리.
 * 한 날짜에 0~23시 중 결제가 발생한 시간만큼 row 가 존재 (aggregate_date + hour 복합 UNIQUE).
 */
public interface DailyHourlySalesRepository extends JpaRepository<DailyHourlySales, Long> {

    /**
     * 특정 날짜의 시간대별 매출을 조회.
     * 결제 발생 시간 수만큼 row 가 반환되므로 List 형태.
     *
     * @param aggregateDate 조회할 집계 날짜
     * @return 해당 날짜의 시간대별 매출 목록 (없으면 빈 List)
     */
    List<DailyHourlySales> findByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 날짜의 dump 가 이미 수행되었는지 확인.
     * 스케줄러/수동 트리거 의 멱등성 체크에 사용.
     *
     * @param aggregateDate 확인할 집계 날짜
     * @return 이미 dump 되었으면 true, 아니면 false
     */
    boolean existsByAggregateDate(LocalDate aggregateDate);
}
