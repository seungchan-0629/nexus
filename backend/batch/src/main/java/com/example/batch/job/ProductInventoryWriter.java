package com.example.batch.job;

import com.example.batch.common.enums.InventoryStatus;
import com.example.batch.common.exception.BaseException;
import com.example.batch.common.model.BaseResponseStatus;
import com.example.batch.domain.head.HeadInventoryRepository;
import com.example.batch.domain.head.model.HeadInventory;
import com.example.batch.domain.inventory.InventoryMovementRepository;
import com.example.batch.domain.inventory.model.InventoryMovement;
import com.example.batch.domain.product.ProductRepository;
import com.example.batch.domain.product.model.Product;
import com.example.batch.domain.store.StoreInventoryRepository;
import com.example.batch.domain.store.StoreRepository;
import com.example.batch.domain.store.model.Store;
import com.example.batch.domain.store.model.StoreInventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductInventoryWriter implements ItemWriter<OrdersItemRow> {

    private final HeadInventoryRepository headInventoryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void write(Chunk<? extends OrdersItemRow> chunk) {
        if (chunk.isEmpty()) return;

        Long productIdx = chunk.getItems().get(0).getProductIdx();
        Product product = productRepository.getReferenceById(productIdx);

        HeadInventory headInventory = headInventoryRepository
                .findByProductIdxForUpdate(productIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.HEAD_INVENTORY_NOT_FOUND));

        List<Long> processedIds = new ArrayList<>();
        for (OrdersItemRow item : chunk) {
            try {
                process(item, headInventory, product);
                processedIds.add(item.getOrdersItemIdx());
            } catch (BaseException e) {
                log.warn("ordersItemIdx={} 처리 실패: {}", item.getOrdersItemIdx(), e.getMessage());
            }
        }

        if (!processedIds.isEmpty()) {
            namedParameterJdbcTemplate.update(
                    "UPDATE orders_item SET processed = true WHERE orders_item_idx IN (:ids)",
                    Map.of("ids", processedIds)
            );
        }
    }

    private void process(OrdersItemRow item, HeadInventory headInventory, Product product) {
        if (headInventory.getCount() < item.getCount()) {
            throw new BaseException(BaseResponseStatus.ORDERS_APPROVE_INSUFFICIENT_STOCK);
        }

        headInventory.setCount(headInventory.getCount() - item.getCount());
        headInventory.setStatus(resolveInventoryStatus(headInventory.getCount(), item.getMinStock()));

        Store store = storeRepository.getReferenceById(item.getStoreIdx());

        storeInventoryRepository.save(new StoreInventory(
                null,
                item.getCount(),
                InventoryStatus.NORMAL,
                headInventory.getManufacturedDate(),
                item.getCount(),
                store,
                product,
                item.getOrdersItemIdx()  // 롤백 식별용
        ));

        inventoryMovementRepository.save(
                InventoryMovement.transferOut(product, item.getStoreIdx(), item.getCount(),
                        "발주 일괄승인 ordersIdx=" + item.getOrdersIdx(), item.getOrdersIdx())  // 롤백 식별용
        );
    }

    private InventoryStatus resolveInventoryStatus(int count, int minStock) {
        if (count <= 0) return InventoryStatus.CRITICAL;
        if (count <= minStock / 2) return InventoryStatus.CRITICAL;
        if (count <= minStock) return InventoryStatus.LOW;
        return InventoryStatus.NORMAL;
    }
}
