package com.example.batch.job;

import com.example.batch.common.enums.OrdersStatus;
import com.example.batch.domain.head.HeadInventoryRepository;
import com.example.batch.domain.inventory.InventoryMovementRepository;
import com.example.batch.domain.orders.OrdersItemRepository;
import com.example.batch.domain.orders.OrdersRepository;
import com.example.batch.domain.orders.model.Orders;
import com.example.batch.domain.orders.model.OrdersItem;
import com.example.batch.domain.store.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RejectInsufficientOrdersService {

    private final OrdersRepository ordersRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final HeadInventoryRepository headInventoryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    /**
     * 재고 부족으로 일부 처리 실패한 주문 1건을 독립 트랜잭션으로 롤백 후 REJECT 처리.
     * <p>
     * 롤백 순서:
     * 1. HeadInventory 재고 복구 – restoreCountByProductIdx() 원자적 UPDATE 사용
     *    (SELECT FOR UPDATE + Java-side modify 제거 → Error 1020 ER_CHECKREAD 방지)
     * 2. StoreInventory 삭제 (orders_item_idx 기준)
     * 3. InventoryMovement 삭제 (orders_idx 기준)
     * 4. ordersItem.processed 리셋
     * 5. orders.reject()
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reject(Long orderId) {
        Orders orders = ordersRepository.lockById(orderId).orElse(null);
        if (orders == null || orders.getOrdersStatus() != OrdersStatus.CONFIRMED) {
            log.warn("orderId={} reject 스킵 (상태 불일치 또는 미존재)", orderId);
            return;
        }

        List<OrdersItem> processedItems = ordersItemRepository.findProcessedByOrdersIdx(orderId);

        if (!processedItems.isEmpty()) {
            for (OrdersItem item : processedItems) {
                int minStock = item.getProduct().getMinStock();

                // 1. HeadInventory 재고 복구 – 단일 테이블 원자적 UPDATE
                //    JOIN 없는 단일 테이블 UPDATE → Error 1020 (ER_CHECKREAD) 방지
                headInventoryRepository.restoreCountByProductIdx(
                        item.getProduct().getIdx(),
                        item.getCount(),
                        minStock / 2,
                        minStock
                );

                // 2. StoreInventory 삭제
                storeInventoryRepository.deleteByOrdersItemIdx(item.getIdx());

                // 3. ordersItem.processed 리셋
                item.resetProcessed();
            }

            // 4. InventoryMovement 삭제 (주문 단위로 한 번에)
            inventoryMovementRepository.deleteByOrdersIdx(orderId);
        }

        // 5. 주문 REJECT
        orders.reject();
        log.info("orderId={} → REJECT (재고 부족, rollback 완료, processedItems={}건)", orderId, processedItems.size());
    }
}
