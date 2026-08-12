package com.example.nexus.domain.delivery;

import com.example.nexus.common.enums.DeliveryStatus;
import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.delivery.model.DeliveryDto;
import com.example.nexus.domain.notification.HeadNotificationService;
import com.example.nexus.domain.notification.StoreNotificationService;
import com.example.nexus.domain.orders.model.Orders;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final HeadNotificationService headNotificationService;
    private final StoreNotificationService storeNotificationService;
    private final StoreRepository storeRepository;

    // 점주 발주 확정 시 배송 생성
    @Transactional
    public void createDelivery(Orders orders) {

        Delivery delivery = Delivery.builder()
                .deliveryStatus(DeliveryStatus.READY)
                .departureDate(LocalDateTime.now())
                .estimatedArrivalAt(LocalDateTime.now().plusDays(1))
                .orders(orders)
                .build();

        deliveryRepository.save(delivery);
    }

    // 본사 발주 승인 시 배송 시작
    @Transactional
    public void startDeliveryByOrders(Orders orders) {

        Delivery delivery = deliveryRepository.findByOrders(orders);

        if (delivery == null ||
                delivery.getDeliveryStatus() != DeliveryStatus.READY) {
            return;
        }

        delivery.setDeliveryStatus(DeliveryStatus.START);

        storeNotificationService.create(
                NotificationType.DELIVERY_START,
                "배송 시작 안내",
                "주문하신 상품의 배송이 시작되었습니다. 예상 도착일: "
                        + delivery.getEstimatedArrivalAt().toLocalDate(),
                orders.getStore()
        );
    }

    // 본사 배송 조회
    public DeliveryDto.DeliveryPageRes getDeliveriesByHead(
            String storeName,
            DeliveryStatus status,
            Integer year,
            Integer month,
            Integer day,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("departureDate").descending()
        );

        Page<Delivery> deliveryPage =
                deliveryRepository.findAllByHeadFilters(
                        storeName,
                        status,
                        year,
                        month,
                        day,
                        pageable
                );

        return DeliveryDto.DeliveryPageRes.from(deliveryPage);
    }

    // 가맹점 배송 조회
    public DeliveryDto.DeliveryPageRes getDeliveriesForStore(
            Long userIdx,
            Long orderIdx,
            DeliveryStatus status,
            Integer year,
            Integer month,
            Integer day,
            int page,
            int size
    ) {

        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("departureDate").descending()
        );

        Page<Delivery> deliveryPage =
                deliveryRepository.findAllByStoreFilters(
                        store.getIdx(),
                        orderIdx,
                        status,
                        year,
                        month,
                        day,
                        pageable
                );

        return DeliveryDto.DeliveryPageRes.from(deliveryPage);
    }

    // 배송 지연 사유 등록
    @Transactional
    public boolean updateDelayReason(
            Long deliveryIdx,
            String delayReason
    ) {

        Delivery delivery = deliveryRepository.findByIdx(deliveryIdx);

        if (delivery == null) {
            return false;
        }

        delivery.setDelayReason(delayReason);
        delivery.setDeliveryStatus(DeliveryStatus.DELAY);

        String delayStoreName =
                delivery.getOrders().getStore().getStoreName();

        // 본사 알림
        headNotificationService.create(
                NotificationType.DELIVERY_DELAY,
                "배송 지연 - " + delayStoreName,
                delayStoreName + " 배송이 지연되었습니다. 사유: " + delayReason
        );

        // 가맹점 알림
        storeNotificationService.create(
                NotificationType.DELIVERY_DELAY,
                "배송 지연 안내",
                "배송이 지연되었습니다. 사유: " + delayReason,
                delivery.getOrders().getStore()
        );

        return true;
    }
}