package com.example.nexus.domain.notification;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.notification.model.StoreNotification;
import com.example.nexus.domain.notification.model.StoreNotificationDto;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.user.model.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreNotificationService {

    private final StoreNotificationRepository storeNotificationRepository;
    private final StoreSseEmitterService storeSseEmitterService;
    private final StoreRepository storeRepository;

    /**
     * 알림 목록 조회 (전체)
     */
    public Slice<StoreNotificationDto.ListRes> getNotifications(AuthUserDetails authUserDetails, int page, int size) {
        Store store = storeRepository.findByUserIdx(authUserDetails.getIdx()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));
        return storeNotificationRepository.findAllByStore_IdxOrderByCreatedAtDesc(store.getIdx(), PageRequest.of(page, size))
                .map(StoreNotificationDto.ListRes::from);
    }

    /**
     * 알림 목록 조회 (읽음 상태별)
     */
    public Slice<StoreNotificationDto.ListRes> getNotificationsByReadStatus(AuthUserDetails authUserDetails, boolean isRead, int page, int size) {
        Store store = storeRepository.findByUserIdx(authUserDetails.getIdx()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));
        return storeNotificationRepository.findAllByStore_IdxAndIsReadOrderByCreatedAtDesc(store.getIdx(), isRead, PageRequest.of(page, size))
                .map(StoreNotificationDto.ListRes::from);
    }

    /**
     * 알림 목록 조회 (타입별 필터)
     */
    public Slice<StoreNotificationDto.ListRes> getNotificationsByType(AuthUserDetails authUserDetails, NotificationType type, int page, int size) {
        Store store = storeRepository.findByUserIdx(authUserDetails.getIdx()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));
        return storeNotificationRepository.findAllByStore_IdxAndTypeOrderByCreatedAtDesc(store.getIdx(), type, PageRequest.of(page, size))
                .map(StoreNotificationDto.ListRes::from);
    }

    /**
     * 미읽음 알림 개수 조회
     */
    public StoreNotificationDto.UnreadCountRes getUnreadCount(AuthUserDetails authUserDetails) {
        Store store = storeRepository.findByUserIdx(authUserDetails.getIdx()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));
        long count = storeNotificationRepository.countByStore_IdxAndIsReadFalse(store.getIdx());
        return StoreNotificationDto.UnreadCountRes.builder().count(count).build();
    }

    /**
     * 단건 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationIdx) {
        StoreNotification notification = storeNotificationRepository.findById(notificationIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_NOTIFICATION));
        notification.markAsRead();
    }

    /**
     * 전체 읽음 처리
     */
    @Transactional
    public void markAllAsRead(AuthUserDetails authUserDetails) {
        Store store = storeRepository.findByUserIdx(authUserDetails.getIdx()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));
        storeNotificationRepository.markAllAsReadByStoreIdx(store.getIdx());
    }

    /**
     * SSE 구독 (실시간 알림 수신)
     */
    public SseEmitter subscribe(AuthUserDetails authUserDetails) {
        Store store = storeRepository.findByUserIdx(authUserDetails.getIdx()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));
        return storeSseEmitterService.subscribe(store.getIdx());
    }

    /**
     * 알림 생성 + SSE 실시간 전송
     */
    @Transactional
    public StoreNotification create(NotificationType type, String title, String content, Store store) {
        StoreNotification notification = StoreNotification.builder()
                .type(type)
                .title(title)
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .store(store)
                .build();
        StoreNotification saved = storeNotificationRepository.save(notification);
        storeSseEmitterService.send(store.getIdx(), StoreNotificationDto.ListRes.from(saved));
        return saved;
    }
}
