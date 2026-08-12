package com.example.pos.event;

// 스토어 메세지 데이터 형식
public record StoreEvent(
        Long storeIdx,
        String storeName,
        boolean isDeleted,
        Long userIdx,
        String address,
        String addressDetail
) {
}
