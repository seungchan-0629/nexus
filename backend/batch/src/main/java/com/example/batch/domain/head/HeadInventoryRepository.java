package com.example.batch.domain.head;

import com.example.batch.domain.head.model.HeadInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeadInventoryRepository extends JpaRepository<HeadInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HeadInventory h WHERE h.product.idx = :productIdx")
    Optional<HeadInventory> findByProductIdxForUpdate(@Param("productIdx") Long productIdx);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HeadInventory h WHERE h.product.idx IN :productIds ORDER BY h.product.idx ASC")
    List<HeadInventory> findAllByProductIdxInForUpdate(@Param("productIds") List<Long> productIds);

    /**
     * 재고 복구 시 사용하는 원자적 단일 테이블 UPDATE.
     * <p>
     * ① SELECT FOR UPDATE + Java-side modify → Error 1020 발생 (커서 스캔 race)
     * ② UPDATE ... JOIN product ... → Error 1020 발생 (멀티테이블 UPDATE read/write 분리 race)
     * ③ 단일 테이블 UPDATE (이 메서드) → Error 1020 없음 (InnoDB row-level atomic)
     * <p>
     * minStock, halfMinStock 은 호출부(Java)에서 product.minStock 으로 계산해 전달한다.
     * <p>
     * SET 절 평가 순서: MariaDB는 SET 절을 좌→우로 순차 평가하므로
     * CASE 를 평가하는 시점의 `count` 는 이미 (old + delta) 인 신규 값이다.
     */
    @Modifying
    @Query(value = """
            UPDATE head_inventory
            SET `count`  = `count` + :delta,
                status = CASE
                    WHEN `count` <= 0             THEN 'CRITICAL'
                    WHEN `count` <= :halfMinStock  THEN 'CRITICAL'
                    WHEN `count` <= :minStock      THEN 'LOW'
                    ELSE 'NORMAL'
                END
            WHERE product_idx = :productIdx
            """, nativeQuery = true)
    void restoreCountByProductIdx(@Param("productIdx") Long productIdx,
                                  @Param("delta") int delta,
                                  @Param("halfMinStock") int halfMinStock,
                                  @Param("minStock") int minStock);
}
