package com.example.nexus.domain.notification;

import com.example.nexus.common.enums.DeliveryStatus;
import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.domain.delivery.DeliveryRepository;
import com.example.nexus.domain.delivery.model.Delivery;
import com.example.nexus.domain.head.HeadInventoryRepository;
import com.example.nexus.domain.head.model.HeadInventory;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.model.StoreInventory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final HeadInventoryRepository headInventoryRepository;
    private final HeadNotificationService headNotificationService;
    private final HeadNotificationRepository headNotificationRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final StoreNotificationService storeNotificationService;
    private final StoreNotificationRepository storeNotificationRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * 유통기한 임박 알림
     * 매일 오전 7시에 실행
     * 제조일 + dangerDays 기준으로 유통기한 3일 이내 제품 감지
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void checkExpiryNotification() {
        // 유통기한 만료까지 며칠 이내일 때 알림을 보낼지 설정
        int alertDays = 3;
        LocalDateTime now = LocalDateTime.now();

        // 재고 수량이 1 이상인 제품만 조회 (재고 없으면 의미 없으므로)
        List<HeadInventory> inventories = headInventoryRepository.findAllByCountGreaterThan(0);

        for (HeadInventory inventory : inventories) {
            // 제품의 유통기한 일수 (예: "30" → 30일)
            int shelfLifeDays = Integer.parseInt(inventory.getProduct().getDangerDays());

            // 유통기한 만료일 = 제조일 + 유통기한 일수
            LocalDateTime expiryDate = inventory.getManufacturedDate().plusDays(shelfLifeDays);

            // 만료일까지 남은 일수 계산
            long daysUntilExpiry = java.time.Duration.between(now, expiryDate).toDays();

            // 만료일이 3일 이내이고, 아직 만료되지 않은 경우에만 알림
            if (daysUntilExpiry <= alertDays && daysUntilExpiry >= 0) {
                String productName = inventory.getProduct().getProductName();
                String title = "유통기한 임박 - " + productName;
                String content = "유통기한 " + daysUntilExpiry + "일 이내입니다. (만료일: "
                        + expiryDate.toLocalDate() + ")";

                // 같은 제품에 대해 오늘 이미 알림을 보냈으면 중복 발송하지 않음
                boolean exists = headNotificationRepository.existsByTypeAndTitleAndCreatedAtAfter(
                        NotificationType.EXPIRY, title, now.toLocalDate().atStartOfDay());
                if (!exists) {
                    headNotificationService.create(NotificationType.EXPIRY, title, content);
                }
            }
        }

        // 가맹점 재고 유통기한 임박 알림 (NOTIFY_008)
        List<StoreInventory> storeInventories = storeInventoryRepository.findAllByCountGreaterThan(0);

        for (StoreInventory storeInventory : storeInventories) {
            int shelfLifeDays = Integer.parseInt(storeInventory.getProduct().getDangerDays());
            LocalDateTime expiryDate = storeInventory.getManufacturedDate().plusDays(shelfLifeDays);
            long daysUntilExpiry = java.time.Duration.between(now, expiryDate).toDays();

            if (daysUntilExpiry <= alertDays && daysUntilExpiry >= 0) {
                String productName = storeInventory.getProduct().getProductName();
                String title = "유통기한 임박 - " + productName;
                String content = "유통기한 " + daysUntilExpiry + "일 이내입니다. (만료일: "
                        + expiryDate.toLocalDate() + ")";

                boolean exists = storeNotificationRepository.existsByTypeAndTitleAndStore_IdxAndCreatedAtAfter(
                        NotificationType.EXPIRY, title, storeInventory.getStore().getIdx(), now.toLocalDate().atStartOfDay());
                if (!exists) {
                    storeNotificationService.create(NotificationType.EXPIRY, title, content, storeInventory.getStore());
                }
            }
        }
    }

    /**
     * 배송 상태 자동 전환
     * 10분마다 실행
     * START + 1시간 경과 → DELIVERYING
     * DELIVERYING + 1일 경과 → DELIVERED + 점주 배송 완료 알림
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void updateDeliveryStatus() {
        LocalDateTime now = LocalDateTime.now();

        // START + 1시간 경과 → DELIVERYING
        List<Delivery> toDelivering = deliveryRepository.findByDeliveryStatusAndDepartureDateBefore(
                DeliveryStatus.START, now.minusHours(1));

        for (Delivery delivery : toDelivering) {
            delivery.setDeliveryStatus(DeliveryStatus.DELIVERYING);
        }

        // DELIVERYING + 1일 경과 → DELIVERED + 점주 배송 완료 알림
        List<Delivery> toDeliver = deliveryRepository.findByDeliveryStatusAndDepartureDateBefore(
                DeliveryStatus.DELIVERYING, now.minusDays(1));

        for (Delivery delivery : toDeliver) {
            delivery.setDeliveryStatus(DeliveryStatus.DELIVERED);
            delivery.setDeliveredDate(now);

            storeNotificationService.create(
                    NotificationType.DELIVERED,
                    "배송 완료 안내",
                    "주문하신 상품의 배송이 완료되었습니다.",
                    delivery.getOrders().getStore());
        }
    }

    /**
     * 배송 지연 자동 감지
     * 매일 오전 8시에 실행
     * 예상도착일 초과 + 미완료 배송 → 점주/운영자 알림
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkDeliveryDelay() {
        LocalDateTime now = LocalDateTime.now();

        List<Delivery> overdueDeliveries = deliveryRepository.findOverdueDeliveries(now, List.of(DeliveryStatus.DELIVERED, DeliveryStatus.DELAY));

        for (Delivery delivery : overdueDeliveries) {
            String storeName = delivery.getOrders().getStore().getStoreName();

            // 본사 알림
            headNotificationService.create(
                    NotificationType.DELIVERY_DELAY,
                    "배송 지연 감지 - " + storeName,
                    storeName + " 배송이 예상 도착일(" + delivery.getEstimatedArrivalAt().toLocalDate() + ")을 초과했습니다.");

            // 가맹점 알림
            storeNotificationService.create(
                    NotificationType.DELIVERY_DELAY,
                    "배송 지연 안내",
                    "예상 도착일(" + delivery.getEstimatedArrivalAt().toLocalDate() + ")이 초과되었습니다. 본사에서 확인 중입니다.",
                    delivery.getOrders().getStore());

            // 지연 처리 완료 → 다음 스케줄러 실행 시 중복 알림 방지
            delivery.setDeliveryStatus(DeliveryStatus.DELAY);
        }
    }

    /**
     * 오래된 알림 자동 삭제
     * 매일 새벽 3시에 실행
     * 읽은 알림: 30일 경과 시 삭제
     * 안 읽은 알림: 90일 경과 시 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime now = LocalDateTime.now();
        headNotificationRepository.deleteReadBefore(now.minusDays(30));
        headNotificationRepository.deleteUnreadBefore(now.minusDays(90));
        storeNotificationRepository.deleteReadBefore(now.minusDays(30));
        storeNotificationRepository.deleteUnreadBefore(now.minusDays(90));
    }

}
