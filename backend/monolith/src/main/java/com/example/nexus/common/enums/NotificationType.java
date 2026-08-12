package com.example.nexus.common.enums;

public enum NotificationType {
    // 본사 알림용
    LOW,            // 재고 부족
    CRITICAL,       // 유통기한 임박
    ABNORMAL,       // 이상 발주
    DELAY,          // 배송 지연
    DELIVERED,      // 배송 완료
    REJECT,         // 발주 거절
    APPROVE,        // 발주 승인

    // 본사 알림용 (신규)
    EXPIRY,         // 유통기한 임박
    LOW_STOCK,      // 재고 부족 (본사)
    ABNORMAL_ORDER, // 비정상 발주 감지
    DELIVERY_DELAY, // 배송 지연 (본사)
    DELIVERY_START  // 배송 시작 (가맹점)
}
