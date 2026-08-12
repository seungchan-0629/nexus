package com.example.nexus.domain.notification;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.domain.notification.model.StoreNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface StoreNotificationRepository extends JpaRepository<StoreNotification, Long> {

    // 알림 목록 전체 조회 (가맹점별, 최신순, 페이징)
    Slice<StoreNotification> findAllByStore_IdxOrderByCreatedAtDesc(Long storeIdx, Pageable pageable);

    // 알림 목록 읽음 상태별 조회 (가맹점별, 최신순, 페이징)
    Slice<StoreNotification> findAllByStore_IdxAndIsReadOrderByCreatedAtDesc(Long storeIdx, boolean isRead, Pageable pageable);

    // 알림 목록 타입별 필터 조회 (가맹점별, 최신순, 페이징)
    Slice<StoreNotification> findAllByStore_IdxAndTypeOrderByCreatedAtDesc(Long storeIdx, NotificationType type, Pageable pageable);

    // 미읽음 알림 개수 조회 (가맹점별)
    long countByStore_IdxAndIsReadFalse(Long storeIdx);

    // 전체 읽음 처리 (가맹점별, 미읽음 → 읽음 일괄 업데이트)
    @Modifying
    @Query("UPDATE StoreNotification n SET n.isRead = true WHERE n.store.idx = :storeIdx AND n.isRead = false")
    void markAllAsReadByStoreIdx(Long storeIdx);

    // 중복 알림 방지: 동일 타입 + 제목 + 가맹점 + 특정 시간 이후 존재 여부 확인
    boolean existsByTypeAndTitleAndStore_IdxAndCreatedAtAfter(NotificationType type, String title, Long storeIdx, LocalDateTime after);

    // 읽은 알림 중 특정 일자 이전 삭제
    @Modifying
    @Query("DELETE FROM StoreNotification n WHERE n.isRead = true AND n.createdAt < :before")
    void deleteReadBefore(LocalDateTime before);

    // 안 읽은 알림 중 특정 일자 이전 삭제
    @Modifying
    @Query("DELETE FROM StoreNotification n WHERE n.isRead = false AND n.createdAt < :before")
    void deleteUnreadBefore(LocalDateTime before);
}
