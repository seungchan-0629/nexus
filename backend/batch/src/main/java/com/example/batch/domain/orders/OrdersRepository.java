package com.example.batch.domain.orders;

import com.example.batch.domain.orders.model.Orders;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orders o WHERE o.idx = :idx")
    Optional<Orders> lockById(@Param("idx") Long idx);

    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.store " +
            "JOIN FETCH o.ordersItemList i " +
            "JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.delivery " +
            "WHERE o.idx = :idx")
    Optional<Orders> findByIdWithDetails(@Param("idx") Long idx);
}
