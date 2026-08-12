package com.example.statistics.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BaseResponseStatus {
    SUCCESS(true, 2000, "요청 성공"),
    FAIL(false, 5000, "요청 실패");

    private final boolean success;
    private final int code;
    private final String message;
}