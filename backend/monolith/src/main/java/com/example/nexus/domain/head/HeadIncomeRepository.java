package com.example.nexus.domain.head;

import com.example.nexus.domain.head.model.HeadIncome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HeadIncomeRepository extends JpaRepository<HeadIncome, Long> {

    List<HeadIncome> findAllByStoreIdx(Long storeIdx);

    List<HeadIncome> findBySettlementIdx(Long idx);

    HeadIncome findByOrdersIdx(Long orderIdx);

    @Query("SELECT hi FROM HeadIncome hi " +
            "JOIN hi.orders o " +
            "WHERE (:storeName IS NULL OR hi.store.storeName LIKE %:storeName%) " +
            "AND (:status IS NULL OR hi.status = :status) " +
            "AND (:startDate IS NULL OR FUNCTION('DATE', o.createdAt) >= :startDate) " +
            "AND (:endDate IS NULL OR FUNCTION('DATE', o.createdAt) <= :endDate)")
    List<HeadIncome> findByFilters(String storeName, Boolean status, LocalDate startDate, LocalDate endDate);

    // 본사 정산 관리 - 가맹점별 그루핑 페이징 조회
    // 가맹점별로 묶어서 발주 건수, 총 정산 금액, 정산 상태를 집계
    @Query("""
            SELECT hi.store.idx, hi.store.storeName, COUNT(hi), SUM(hi.price),
                   CASE WHEN SUM(CASE WHEN hi.status = false THEN 1 ELSE 0 END) > 0 THEN false ELSE true END
            FROM HeadIncome hi
            JOIN hi.orders o
            WHERE (:storeName IS NULL OR :storeName = '' OR hi.store.storeName LIKE %:storeName%)
              AND o.createdAt >= :start
              AND o.createdAt < :end
            GROUP BY hi.store.idx, hi.store.storeName
            ORDER BY hi.store.storeName ASC
            """)
    Page<Object[]> findStoreSettlementByMonth(
            @Param("storeName") String storeName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 본사 정산 관리 - 상단 카드용 전체 청구 합계
    @Query("""
            SELECT COALESCE(SUM(hi.price), 0)
            FROM HeadIncome hi
            JOIN hi.orders o
            WHERE (:storeName IS NULL OR :storeName = '' OR hi.store.storeName LIKE %:storeName%)
              AND o.createdAt >= :start
              AND o.createdAt < :end
            """)
    Long sumTotalBillingByMonth(
            @Param("storeName") String storeName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT hi
            FROM HeadIncome hi
            WHERE hi.store.idx = :storeIdx
              AND hi.status = false
              AND hi.orders.createdAt >= :start
              AND hi.orders.createdAt < :end
            """)
    List<HeadIncome> findUnpaidByStoreAndMonth(
            @Param("storeIdx") Long storeIdx,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    }