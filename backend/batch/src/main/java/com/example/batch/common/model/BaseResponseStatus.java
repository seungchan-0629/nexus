package com.example.batch.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BaseResponseStatus {
    SUCCESS(true, 2000, "요청 성공"),
    REQUEST_ERROR(false, 4001, "입력값이 잘못되었습니다."),
    NOT_FOUND_PRODUCT(false, 3202, "제품을 찾을 수 없습니다."),
    HEAD_INVENTORY_NOT_FOUND(false, 3305, "본사 재고 항목을 찾을 수 없습니다."),
    STORE_INVENTORY_INSUFFICIENT(false, 3306, "본사 재고가 부족합니다."),
    ORDERS_APPROVE_INSUFFICIENT_STOCK(false, 4201, "본사 재고가 부족하여 발주를 승인할 수 없습니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;
}
