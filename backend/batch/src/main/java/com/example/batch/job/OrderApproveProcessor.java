package com.example.batch.job;

import com.example.batch.common.enums.OrdersStatus;
import com.example.batch.domain.orders.OrdersRepository;
import com.example.batch.domain.orders.model.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApproveProcessor implements ItemProcessor<Long, Orders> {

    private final OrdersRepository ordersRepository;

    @Override
    public Orders process(Long orderId) {
        log.info("processing orderId={}", orderId);
        return ordersRepository.lockById(orderId)
                .filter(o -> o.getOrdersStatus() == OrdersStatus.CONFIRMED)
                .flatMap(o -> ordersRepository.findByIdWithDetails(orderId))
                .orElse(null);
    }
}
