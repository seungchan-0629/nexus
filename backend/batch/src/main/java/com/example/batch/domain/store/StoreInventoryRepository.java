package com.example.batch.domain.store;

import com.example.batch.domain.store.model.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM StoreInventory s WHERE s.ordersItemIdx = :ordersItemIdx")
    void deleteByOrdersItemIdx(@Param("ordersItemIdx") Long ordersItemIdx);
}
