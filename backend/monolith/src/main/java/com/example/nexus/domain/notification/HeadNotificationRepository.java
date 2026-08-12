package com.example.nexus.domain.notification;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.domain.notification.model.HeadNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface HeadNotificationRepository extends JpaRepository<HeadNotification, Long> {

    // 알림 목록 전체 조회 (최신순, 페이징)
    Slice<HeadNotification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 알림 목록 읽음 상태별 조회 (최신순, 페이징)
    Slice<HeadNotification> findAllByIsReadOrderByCreatedAtDesc(boolean isRead, Pageable pageable);

    // 알림 목록 타입별 필터 조회 (최신순, 페이징)
    Slice<HeadNotification> findAllByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);

    // 미읽음 알림 개수 조회
    long countByIsReadFalse();

    // 전체 읽음 처리 (미읽음 → 읽음 일괄 업데이트)
    @Modifying
    @Query("UPDATE HeadNotification n SET n.isRead = true WHERE n.isRead = false")
    void markAllAsRead();

    // 중복 알림 방지: 동일 타입 + 제목 + 특정 시간 이후 존재 여부 확인
    boolean existsByTypeAndTitleAndCreatedAtAfter(NotificationType type, String title, LocalDateTime after);

    // 읽은 알림 중 특정 일자 이전 삭제
    @Modifying
    @Query("DELETE FROM HeadNotification n WHERE n.isRead = true AND n.createdAt < :before")
    void deleteReadBefore(LocalDateTime before);

    // 안 읽은 알림 중 특정 일자 이전 삭제
    @Modifying
    @Query("DELETE FROM HeadNotification n WHERE n.isRead = false AND n.createdAt < :before")
    void deleteUnreadBefore(LocalDateTime before);
}
