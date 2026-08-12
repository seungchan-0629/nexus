package com.example.nexus.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BaseResponseStatus {
    // 2000번대 성공
    SUCCESS(true, 2000, "요청 성공"),

    // 3000번대 클라이언트 입력 오류, 입력값 검증 오류
    JWT_EXPIRED(false, 3001, "JWT 토큰 만료"),
    JWT_INVALID(false, 3002, "JWT 토큰 유효하지 않음"),
    SIGNUP_DUPLICATE_EMAIL(false, 3003, "중복된 이메일"),
    SIGNUP_INVALID_PASSWORD(false, 3004, "비밀번호는 대소문자, 숫자, 특수문자 포함"),
    SIGNUP_INVALID_UUID(false, 3005, "유효하지 않은 인증값"),
    USER_NOT_FOUND(false, 3006, "존재하지 않는 사용자입니다."),
    PASSWORD_WRONG(false, 3007, "비밀번호가 일치하지 않습니다."),


    // 3100번대 ~ 가맹점 클라이언트 오류
    NOT_FOUND_USER(false, 3101, "등록되지 않은 점주 정보입니다."),
    STORE_NAME_ALREADY_EXISTS(false, 3102, "이미 사용중인 가맹점명입니다."),
    BUSINESS_NUMBER_ALREADY_EXISTS(false, 3103, "이미 등록된 사업자 번호입니다."),
    ALREADY_HAS_STORE(false, 3104, "해당 점주는 이미 등록된 가맹점이 존재합니다."),
    STORE_NOT_FOUND(false, 3105, "해당 가맹점 정보를 찾을 수 없습니다."),
    STORE_ALREADY_CLOSED(false, 3106, "폐업 처리되어 수정할 수 없는 가맹점입니다."),
    DUPLICATE_EMAIL(false, 3107, "이미 다른 계정에서 사용중인 이메일입니다."),
    S3_DELETE_FAILED(false, 3108, "S3 삭제 실패했습니다."),
    EXCEED_PDF_FILE_SIZE(false, 3109, "파일 크기는 10MB를 초과할 수 없습니다."),

    // 3200번대 ~ 메뉴 클라이언트 오류
    NOT_FOUND_CATEGORY(false, 3201, "존재하지 않는 카테고리입니다."),
    NOT_FOUND_PRODUCT(false, 3202, "제품을 찾을 수 없습니다."),
    DUPLICATE_MENU_NAME(false, 3203, "이미 사용중인 메뉴명입니다."),
    NOT_FOUND_MENU(false, 3204, "존재하지 않는 메뉴입니다."),
    DUPLICATE_CATEGORY_NAME(false, 3205, "이미 존재하는 카테고리입니다."),
    CATEGORY_IN_USE(false, 3206, "해당 카테고리를 사용하는 메뉴가 존재하여 삭제할 수 없습니다."),
    EXCEED_IMG_FILE_SIZE(false, 3207, "파일 크기는 5MB를 초과할 수 없습니다."),
    DELETED_MENU_NAME(false, 3208, "과거에 사용 후 삭제된 메뉴명은 재사용이 불가능합니다."),


    // 4000번대 실패
    REQUEST_ERROR(false, 4001, "입력값이 잘못되었습니다."),
    NOT_FOUND_DATA(false, 4002, "데이터를 찾을 수 없습니다."),
    ALREADY_JOINED(false, 4003, "이미 참여 중인 모집글이 있습니다."),
    RECRUIT_FULL(false, 4004, "모집이 마감되었거나 인원이 초과되었습니다."),
    ALREADY_CALLED(false, 4005, "이미 기사님 호출이 완료되어 나갈 수 없습니다."),
    LOGIN_FAILED(false, 4006, "로그인에 실패하였습니다."),
    DRIVER_ROLE_REQUIRED(false, 4007, "드라이버 계정만 로그인할 수 있습니다."),
    ADMIN_ONLY_ACCESS(false, 4008, "관리자 계정만 공지사항 작업을 할 수 있습니다."),

    // 3300번대 ~ POS / 재고 클라이언트 오류
    POS_INVENTORY_NOT_FOUND(false, 3301, "POS 재고 항목을 찾을 수 없습니다."),
    STORE_INVENTORY_NOT_FOUND(false, 3302, "가맹점 재고 항목을 찾을 수 없습니다."),
    STORE_INVENTORY_NOT_AUTHORIZED(false, 3303, "해당 재고에 대한 권한이 없습니다."),
    WASTE_QUANTITY_EXCEEDS_STOCK(false, 3304, "폐기 수량이 현재 재고 수량을 초과합니다."),
    HEAD_INVENTORY_NOT_FOUND(false, 3305, "본사 재고 항목을 찾을 수 없습니다."),
    STORE_INVENTORY_INSUFFICIENT(false, 3306, "본사 재고가 부족합니다."),
    POS_STORE_INVENTORY_INSUFFICIENT(false, 3307, "POS 재고가 부족합니다."),

    // 4100번대~ 결제 관련
    PAYMENT_UNAUTHENTICATED_USER(false, 4100, "인증받지 않은 사용자입니다."),
    PAYMENT_ENROLL_INVALID_USER(false, 4101, "결제 수단을 등록할 수 없는 사용자입니다."),
    PAYMENT_ENROLL_INVALID_CUSTOMER_KEY(false, 4102, "고객 키가 일치하지 않습니다."),
    PAYMENT_BILLING_NOT_EXIST(false, 4103, "존재하지 않는 결제 수단입니다."),
    PAYMENT_BILLING_INVALID_OWNER(false, 4104, "결제 수단의 소유자가 아닙니다."),
    PAYMENT_BILLING_REQUIRED(false, 4105, "최소 1개의 결제 수단이 필요합니다."),
    PAYMENT_DEFAULT_BILLING_REQUIRED(false, 4106, "기본 결제 수단이 존재하지 않습니다."),

    // 4200번대 ~ 발주 관련
    ORDERS_APPROVE_INSUFFICIENT_STOCK(false, 4201, "본사 재고가 부족하여 발주를 승인할 수 없습니다."),
    NOT_FOUND_ORDERS(false, 4202, "발주를 찾을 수 없습니다."),
    NOT_FOUND_ORDERS_ITEM(false, 4203, "발주 항목을 찾을 수 없습니다."),
    ORDERS_NOT_AUTHORIZED(false, 4204, "해당 발주에 대한 권한이 없습니다."),
    ORDERS_INVALID_STATUS(false, 4205, "현재 발주 상태에서 처리할 수 없습니다."),
    ORDERS_ITEMS_EMPTY(false, 4209, "발주 항목이 비어 있습니다."),
    BATCH_SERVICE_UNAVAILABLE(false, 4210, "배치 서비스 호출에 실패했습니다."),

    // 4300번대 ~ 알림 관련
    NOT_FOUND_NOTIFICATION(false, 4301, "알림을 찾을 수 없습니다."),

    // 5000번대 실패
    FAIL(false, 5000, "요청 실패"),
    DATABASE_ERROR(false, 5001, "데이터베이스 연결 및 처리 오류");

    private final boolean success;
    private final int code;
    private final String message;
}
