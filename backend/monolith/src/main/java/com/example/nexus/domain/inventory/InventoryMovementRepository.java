package com.example.nexus.domain.inventory;

import com.example.nexus.domain.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // 목록 조회 시 Product N+1 방지
    @Query("SELECT m FROM InventoryMovement m JOIN FETCH m.product ORDER BY m.idx ASC")
    List<InventoryMovement> findAllWithProduct();
}
