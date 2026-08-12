package com.example.statistics.domain.daily;

import com.example.statistics.domain.daily.model.DailyDumpLog;
import com.example.statistics.domain.daily.model.DumpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 일별 dump 이력 관리 저장소.
 * 매일 새벽 5시 스케줄러 또는 수동 트리거가 dump 를 수행한 결과를 기록.
 * 운영 도구로서 누락 복구 + 모니터링 + 디버깅 용도.
 */
public interface DailyDumpLogRepository extends JpaRepository<DailyDumpLog, Long> {

    /**
     * 특정 날짜의 dump 이력을 조회.
     * 재시도/멱등성 판단, 운영 디버깅에 사용.
     *
     * @param aggregateDate 조회할 집계 날짜
     * @return 해당 날짜의 dump 이력 (수행되지 않았으면 Optional.empty())
     */
    Optional<DailyDumpLog> findByAggregateDate(LocalDate aggregateDate);

    /**
     * 특정 날짜에 특정 상태(SUCCESS/FAILED)의 dump 이력이 있는지 확인.
     * 예: "이 날짜 SUCCESS 됐나?" 빠른 체크 — 스케줄러 재실행 시 SUCCESS 인 날짜는 skip.
     *
     * @param aggregateDate 확인할 집계 날짜
     * @param status        확인할 dump 상태 (SUCCESS / FAILED)
     * @return 해당 상태로 기록되어 있으면 true, 아니면 false
     */
    boolean existsByAggregateDateAndDumpStatus(LocalDate aggregateDate, DumpStatus status);

    /**
     * 마지막으로 SUCCESS 처리된 dump 날짜를 조회.
     * 누락 복구 로직의 기준점 — 스케줄러는 이 날짜 + 1 ~ 어제 까지 catch-up 처리.
     *
     * 예시) 마지막 성공일이 2026-05-19, 오늘이 2026-05-22 라면
     *      → 2026-05-20, 2026-05-21 두 날짜를 추가로 dump 수행
     *
     * @return 마지막 성공 dump 날짜 (한 번도 SUCCESS 없으면 Optional.empty())
     */
    @Query("SELECT MAX(d.aggregateDate) FROM DailyDumpLog d WHERE d.dumpStatus = 'SUCCESS'")
    Optional<LocalDate> findLastSuccessfulDumpDate();
}
