package com.example.nexus.domain.notification;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.notification.model.HeadNotification;
import com.example.nexus.domain.notification.model.HeadNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadNotificationService {

    private final HeadNotificationRepository headNotificationRepository;
    private final SseEmitterService sseEmitterService;

    /**
     * 알림 목록 조회 (전체)
     */
    public Slice<HeadNotificationDto.ListRes> getNotifications(int page, int size) {
        return headNotificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(HeadNotificationDto.ListRes::from);
    }

    /**
     * 알림 목록 조회 (읽음 상태별)
     */
    public Slice<HeadNotificationDto.ListRes> getNotificationsByReadStatus(boolean isRead, int page, int size) {
        return headNotificationRepository.findAllByIsReadOrderByCreatedAtDesc(isRead, PageRequest.of(page, size))
                .map(HeadNotificationDto.ListRes::from);
    }

    /**
     * 알림 목록 조회 (타입별 필터)
     */
    public Slice<HeadNotificationDto.ListRes> getNotificationsByType(NotificationType type, int page, int size) {
        return headNotificationRepository.findAllByTypeOrderByCreatedAtDesc(type, PageRequest.of(page, size))
                .map(HeadNotificationDto.ListRes::from);
    }

    /**
     * 미읽음 알림 개수 조회
     */
    public HeadNotificationDto.UnreadCountRes getUnreadCount() {
        long count = headNotificationRepository.countByIsReadFalse();
        return HeadNotificationDto.UnreadCountRes.builder().count(count).build();
    }

    /**
     * 단건 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationIdx) {
        HeadNotification notification = headNotificationRepository.findById(notificationIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_NOTIFICATION));
        notification.markAsRead();
    }

    /**
     * 전체 읽음 처리
     */
    @Transactional
    public void markAllAsRead() {
        headNotificationRepository.markAllAsRead();
    }

    /**
     * 알림 생성 + SSE 실시간 전송
     */
    @Transactional
    public HeadNotification create(NotificationType type, String title, String content) {
        HeadNotification notification = HeadNotification.builder()
                .type(type)
                .title(title)
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        HeadNotification saved = headNotificationRepository.save(notification);
        sseEmitterService.broadcast(HeadNotificationDto.ListRes.from(saved));
        return saved;
    }
}
