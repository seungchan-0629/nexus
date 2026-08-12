package com.example.nexus.domain.pos;

import com.example.nexus.domain.pos.model.PosStoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PosStoreInventoryRepository extends JpaRepository<PosStoreInventory, Long> {
    // 목록 조회 시 Store·Product N+1 방지
    @Query("""
            SELECT p FROM PosStoreInventory p
            JOIN FETCH p.store
            JOIN FETCH p.product
            WHERE p.store.idx = :storeIdx
            """)
    List<PosStoreInventory> findByStoreIdxWithStoreAndProduct(@Param("storeIdx") Long storeIdx);

    @Query(
            value = """
                    SELECT DISTINCT p.product.idx
                    FROM PosStoreInventory p
                    WHERE p.store.idx = :storeIdx
                    ORDER BY p.product.idx
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.product.idx)
                    FROM PosStoreInventory p
                    WHERE p.store.idx = :storeIdx
                    """
    )
    Page<Long> findPagedProductIdsByStoreIdx(@Param("storeIdx") Long storeIdx, Pageable pageable);

    @Query("""
            SELECT p FROM PosStoreInventory p
            JOIN FETCH p.store
            JOIN FETCH p.product
            WHERE p.store.idx = :storeIdx
              AND p.product.idx IN :productIds
            ORDER BY p.product.idx, p.manufacturedDate
            """)
    List<PosStoreInventory> findByStoreIdxAndProductIds(
            @Param("storeIdx") Long storeIdx,
            @Param("productIds") List<Long> productIds
    );

    // 결제 FIFO 차감 시 동시성 제어
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p FROM PosStoreInventory p
            WHERE p.store.idx = :storeIdx
              AND p.product.idx = :productIdx
            ORDER BY p.manufacturedDate ASC
            """)
    List<PosStoreInventory> findByStoreAndProductForUpdate(
            @Param("storeIdx") Long storeIdx,
            @Param("productIdx") Long productIdx
    );

    Optional<PosStoreInventory> findByStore_IdxAndProduct_IdxAndManufacturedDate(Long storeIdx, Long productIdx, LocalDateTime manufacturedDate);
}
