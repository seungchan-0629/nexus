package com.example.nexus.domain.notification.model;

import com.example.nexus.common.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class StoreNotificationDto {

    @Getter
    @Builder
    public static class ListRes {
        private Long idx;
        private NotificationType type;
        private String title;
        private String content;
        @JsonProperty("isRead")
        private boolean isRead;
        private LocalDateTime createdAt;

        public static ListRes from(StoreNotification entity) {
            return ListRes.builder()
                    .idx(entity.getIdx())
                    .type(entity.getType())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .isRead(entity.isRead())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class UnreadCountRes {
        private long count;
    }
}
