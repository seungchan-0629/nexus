package com.example.batch.domain.inventory;

import com.example.batch.domain.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM InventoryMovement m WHERE m.ordersIdx = :ordersIdx")
    void deleteByOrdersIdx(@Param("ordersIdx") Long ordersIdx);
}
