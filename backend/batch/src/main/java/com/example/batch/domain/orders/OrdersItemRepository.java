package com.example.batch.domain.orders;

import com.example.batch.domain.orders.model.OrdersItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdersItemRepository extends JpaRepository<OrdersItem, Long> {

    /**
     * 특정 주문에서 처리 완료된(processed=true) ordersItem을 product까지 fetch해서 조회.
     * RejectInsufficientOrdersService에서 rollback 대상 식별에 사용.
     */
    @Query("SELECT oi FROM OrdersItem oi JOIN FETCH oi.product WHERE oi.orders.idx = :ordersIdx AND oi.processed = true")
    List<OrdersItem> findProcessedByOrdersIdx(@Param("ordersIdx") Long ordersIdx);
}
