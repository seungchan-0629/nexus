package com.example.nexus.domain.head;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.domain.head.model.HeadInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeadInventoryRepository extends JpaRepository<HeadInventory, Long> {
    // 본사 재고 조회 & N+1 방지
    @Query("SELECT h FROM HeadInventory h JOIN FETCH h.product")
    List<HeadInventory> findAllWithProduct();

    @EntityGraph(attributePaths = "product")
    Page<HeadInventory> findAllBy(Pageable pageable);

    // 입·출고 등 count 갱신 시 동시성 제어
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HeadInventory h WHERE h.product.idx = :productIdx")
    Optional<HeadInventory> findByProductIdxForUpdate(@Param("productIdx") Long productIdx);

    // 대시보드용: 위험 재고 건수 조회
    long countByStatus(InventoryStatus status);

    // 대시보드용: 위험 재고 목록 조회 (Slice 페이징)
    @Query("SELECT h FROM HeadInventory h JOIN FETCH h.product " +
            "WHERE h.status IN :statuses")
    Slice<HeadInventory> findByStatusIn(@Param("statuses") List<InventoryStatus> statuses, Pageable pageable);

    // 알림용: 재고가 있는 전체 목록 조회
    List<HeadInventory> findAllByCountGreaterThan(int count);

    // 유통기한 기준 status 일괄 갱신
    @Modifying
    @Query(value = """
            UPDATE head_inventory hi
            JOIN product p ON hi.product_idx = p.product_idx
            SET hi.status = CASE
                WHEN DATEDIFF(NOW(), hi.manufactured_date) >= CAST(p.danger_days AS UNSIGNED)
                    THEN 'EXPIRED'
                WHEN DATEDIFF(NOW(), hi.manufactured_date) >= CAST(p.danger_days AS UNSIGNED) - 3
                    THEN 'EXPIRING'
                ELSE hi.status
            END
            WHERE hi.count > 0
            """, nativeQuery = true)
    void bulkUpdateExpiryStatus();
}
