package com.example.pos.common.exception;

import com.example.pos.common.model.BaseResponseStatus;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final BaseResponseStatus status;
    /** 실패 응답의 {@code result}에 실을 추가 정보(예: 재고 부족 시 productIdx, 수량). */
    private final Object result;

    public BaseException(BaseResponseStatus status) {
        this(status, null);
    }

    public BaseException(BaseResponseStatus status, Object result) {
        super(status.getMessage());
        this.status = status;
        this.result = result;
    }

    public static BaseException from(BaseResponseStatus status) {
        return new BaseException(status);
    }
}
