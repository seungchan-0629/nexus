package com.example.nexus.domain.store;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.domain.store.model.StoreInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    List<StoreInventory> findByStoreIdx(Long storeIdx);

    @Query(
            value = """
                    SELECT DISTINCT s.product.idx
                    FROM StoreInventory s
                    WHERE s.store.idx = :storeIdx
                    ORDER BY s.product.idx
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT s.product.idx)
                    FROM StoreInventory s
                    WHERE s.store.idx = :storeIdx
                    """
    )
    Page<Long> findPagedProductIdsByStoreIdx(@Param("storeIdx") Long storeIdx, Pageable pageable);

    @Query("""
            SELECT s FROM StoreInventory s
            JOIN FETCH s.store
            JOIN FETCH s.product
            WHERE s.store.idx = :storeIdx
              AND s.product.idx IN :productIds
            ORDER BY s.product.idx, s.manufacturedDate
            """)
    List<StoreInventory> findByStoreIdxAndProductIds(@Param("storeIdx") Long storeIdx, @Param("productIds") List<Long> productIds);

    // 점주 대시보드용 - 재고 위험 품목 KPI 카드 (상태별 개수)
    long countByStore_IdxAndStatus(Long storeIdx, InventoryStatus status);

    // 점주 대시보드용 - 재고 위험 품목 목록 (CRITICAL/LOW 상태 조회)
    @Query("SELECT s FROM StoreInventory s JOIN FETCH s.product " +
            "WHERE s.store.idx = :storeIdx AND s.status IN :statuses " +
            "ORDER BY s.status DESC")
    List<StoreInventory> findByStore_IdxAndStatusInOrderByStatusDesc(
            @Param("storeIdx") Long storeIdx,
            @Param("statuses") List<InventoryStatus> statuses);

    Optional<StoreInventory> findByStore_IdxAndProduct_IdxAndManufacturedDate(Long storeIdx, Long productIdx, LocalDateTime manufacturedDate);

    List<StoreInventory> findByStore_IdxAndProduct_IdxOrderByManufacturedDateAsc(Long storeIdx, Long productIdx);

    // 유통기한 임박 알림용 - 재고 수량이 0보다 큰 가맹점 재고 조회
    List<StoreInventory> findAllByCountGreaterThan(int count);
           
    List<StoreInventory> findAllByManufacturedDateBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    // 유통기한 기준 status 일괄 갱신
    @Modifying
    @Query(value = """
            UPDATE store_inventory si
            JOIN product p ON si.product_idx = p.product_idx
            SET si.status = CASE
                WHEN DATEDIFF(NOW(), si.manufactured_date) >= CAST(p.danger_days AS UNSIGNED)
                    THEN 'EXPIRED'
                WHEN DATEDIFF(NOW(), si.manufactured_date) >= CAST(p.danger_days AS UNSIGNED) - 3
                    THEN 'EXPIRING'
                ELSE si.status
            END
            WHERE si.count > 0
            """, nativeQuery = true)
    void bulkUpdateExpiryStatus();
}
