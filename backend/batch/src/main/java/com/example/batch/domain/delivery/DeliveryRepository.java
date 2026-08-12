package com.example.batch.domain.delivery;

import com.example.batch.domain.delivery.model.Delivery;
import com.example.batch.domain.orders.model.Orders;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Delivery d WHERE d.orders = :orders")
    Delivery findByOrdersForUpdate(@Param("orders") Orders orders);

    /**
     * Step2 배치 최적화용: 청크 단위로 여러 주문의 Delivery를 한 번에 조회.
     * 개별 findByOrdersForUpdate() N회 → 이 메서드 1회로 대체.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Delivery d JOIN FETCH d.orders o WHERE o.idx IN :orderIds")
    List<Delivery> findAllByOrdersIdxIn(@Param("orderIds") List<Long> orderIds);
}
