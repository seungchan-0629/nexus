package com.example.pos.event;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentEvent(
        Long posPayIdx,
        Long storeIdx,
        String storeName,
        String method,            // "CARD" or "CASH"
        LocalDateTime paidAt,
        Long payAmount,
        List<PaymentEventItem> items
) {}