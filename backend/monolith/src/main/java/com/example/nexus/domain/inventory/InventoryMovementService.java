package com.example.nexus.domain.inventory;

import com.example.nexus.common.enums.InventoryStatus;
import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.domain.head.HeadInventoryRepository;
import com.example.nexus.domain.head.model.HeadInventory;
import com.example.nexus.domain.inventory.model.InventoryMovement;
import com.example.nexus.domain.inventory.model.InventoryMovementDto;
import com.example.nexus.domain.notification.HeadNotificationRepository;
import com.example.nexus.domain.notification.HeadNotificationService;
import com.example.nexus.domain.notification.StoreNotificationService;
import com.example.nexus.domain.product.ProductRepository;
import com.example.nexus.domain.product.model.Product;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.store.model.StoreInventory;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nexus.common.enums.InventoryStatus;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {
    private final InventoryMovementRepository inventoryMovementRepository;
    private final HeadInventoryRepository headInventoryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final HeadNotificationService headNotificationService;
    private final HeadNotificationRepository headNotificationRepository;
    private final StoreNotificationService storeNotificationService;

    @Transactional
    public InventoryMovementDto.MovementRes inbound(InventoryMovementDto.InboundReq req) {

        Product product = productRepository.findById(req.getProductIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_PRODUCT));

        HeadInventory headInventory = headInventoryRepository.findByProductIdxForUpdate(product.getIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.HEAD_INVENTORY_NOT_FOUND));

        headInventory.setCount(headInventory.getCount() + req.getQuantity());
        headInventory.setStatus(resolveStatus(headInventory.getCount(), product.getMinStock()));

        headInventoryRepository.save(headInventory);

        InventoryMovement movement = InventoryMovement.inbound(
                product,
                req.getQuantity(),
                req.getMemo()
        );
        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);

        return InventoryMovementDto.MovementRes.from(savedMovement);
    }

    @Transactional
    public InventoryMovementDto.MovementRes outbound(InventoryMovementDto.OutboundReq req) {

        Product product = productRepository.findById(req.getProductIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_PRODUCT));

        Store store = storeRepository.findById(req.getStoreIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        HeadInventory headInventory = headInventoryRepository.findByProductIdxForUpdate(product.getIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.HEAD_INVENTORY_NOT_FOUND));

        if (headInventory.getCount() < req.getQuantity()) {
            throw new BaseException(BaseResponseStatus.STORE_INVENTORY_INSUFFICIENT);
        }

        headInventory.setCount(headInventory.getCount() - req.getQuantity());
        headInventory.setStatus(resolveStatus(headInventory.getCount(), product.getMinStock()));
        headInventoryRepository.save(headInventory);

        // 재고 부족 알림: 차감 후 최소 기준 이하이면 알림 발송 (당일 동일 제품 중복 방지)
        if (headInventory.getCount() <= product.getMinStock()) {
            String title = "재고 부족 - " + product.getProductName();
            boolean alreadySent = headNotificationRepository.existsByTypeAndTitleAndCreatedAtAfter(
                    NotificationType.LOW_STOCK, title, LocalDate.now().atStartOfDay());
            if (!alreadySent) {
                headNotificationService.create(
                        NotificationType.LOW_STOCK,
                        title,
                        "현재 수량: " + headInventory.getCount() + product.getProductUnit()
                                + " (최소 기준: " + product.getMinStock() + product.getProductUnit() + ")");
            }

            // 가맹점 재고 부족 알림 (NOTIFY_009)
            storeNotificationService.create(
                    NotificationType.LOW_STOCK,
                    title,
                    product.getProductName() + " 재고가 부족합니다. 본사에 발주를 요청해주세요.",
                    store);
        }

        StoreInventory storeInventory = new StoreInventory(
                null,
                req.getQuantity(),
                InventoryStatus.NORMAL,
                headInventory.getManufacturedDate(),
                req.getQuantity(),
                store,
                product,
                null   // ordersItemIdx: 수동 이동은 발주와 무관
        );

        storeInventoryRepository.save(storeInventory);

        InventoryMovement movement = InventoryMovement.transferOut(
                product,
                store.getIdx(),
                req.getQuantity(),
                req.getMemo()
        );
        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);

        return InventoryMovementDto.MovementRes.from(savedMovement);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementDto.MovementListRes> findAllMovements() {

        List<InventoryMovement> inventoryMovementList = inventoryMovementRepository.findAllWithProduct();

        return inventoryMovementList.stream().map(InventoryMovementDto.MovementListRes::from).toList();
    }

    private InventoryStatus resolveStatus(int count, int minStock) {
        if (count <= 0) return InventoryStatus.CRITICAL;
        if (count <= minStock / 2) return InventoryStatus.CRITICAL;
        if (count <= minStock) return InventoryStatus.LOW;
        return InventoryStatus.NORMAL;
    }
}
