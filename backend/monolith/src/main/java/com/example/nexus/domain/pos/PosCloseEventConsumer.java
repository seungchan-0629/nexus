package com.example.nexus.domain.pos;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.model.StoreInventory;
import com.example.nexus.event.KafkaTopics;
import com.example.nexus.event.PosCloseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PosCloseEventConsumer {

    private final AutoOrderService autoOrderService;
    private final StoreInventoryRepository storeInventoryRepository;

    @Transactional
    @KafkaListener(
        topics = KafkaTopics.POS_CLOSE_COMPLETED,
        groupId = "monolith-autoorder-group"
    )
    public void onPosClose(PosCloseEvent event) {
        log.info("[Kafka Consumer] POS 마감 이벤트 수신 - 본사 재고 차감 및 AI 자동 발주 시작: storeIdx={}", event.storeIdx());
        
        // 1. 본사 재고 차감 로직 수행 (FIFO 방식)
        if (event.productNeed() != null) {
            for (Map.Entry<Long, Integer> entry : event.productNeed().entrySet()) {
                deductOfficialFifo(event.storeIdx(), entry.getKey(), entry.getValue());
            }
        }
        
        // AI 자동 발주 수행
        autoOrderService.generateAutoOrder(event.userIdx());
    }


    private void deductOfficialFifo(Long storeIdx, Long productIdx, int amount) {
        if (amount <= 0) return;
        
        List<StoreInventory> lots = storeInventoryRepository
                .findByStore_IdxAndProduct_IdxOrderByManufacturedDateAsc(storeIdx, productIdx);
        
        int availableTotal = lots.stream().mapToInt(StoreInventory::getCount).sum();
        if (availableTotal < amount) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("productIdx", productIdx);
            detail.put("required", amount);
            detail.put("availableTotal", availableTotal);
            log.warn("본사 재고 부족: productIdx={}, required={}, available={}", productIdx, amount, availableTotal);
            throw new BaseException(BaseResponseStatus.STORE_INVENTORY_INSUFFICIENT, detail);
        }
        
        int remaining = amount;
        for (StoreInventory lot : lots) {
            if (remaining <= 0) break;
            
            int onHand = lot.getCount();
            if (onHand <= 0) continue;
            
            int take = Math.min(onHand, remaining);
            lot.setCount(onHand - take);
            lot.setStatus(resolveStatus(lot.getCount(), lot.getProduct().getMinStock()));
            remaining -= take;
        }
    }

    private InventoryStatus resolveStatus(int count, int minStock) {
        if (count <= 0) return InventoryStatus.CRITICAL;
        if (count <= minStock / 2) return InventoryStatus.CRITICAL;
        if (count <= minStock) return InventoryStatus.LOW;
        return InventoryStatus.NORMAL;
    }
}
