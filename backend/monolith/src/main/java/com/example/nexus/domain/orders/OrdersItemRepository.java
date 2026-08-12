package com.example.nexus.domain.orders;

import com.example.nexus.domain.orders.model.OrdersItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface OrdersItemRepository extends JpaRepository<OrdersItem, Long> {

    // 발주용 - 특정 발주서의 품목 목록 조회
    List<OrdersItem> findByOrdersIdx(Long ordersIdx);

    List<OrdersItem> findByOrdersIdxIn(List<Long> ordersIdxList);
}
