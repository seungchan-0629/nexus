package com.example.nexus.domain.notification.model;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_notification_idx")
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_idx", nullable = false)
    private Store store;

    public void markAsRead() {
        this.isRead = true;
    }
}
