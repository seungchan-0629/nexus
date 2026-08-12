package com.example.pos.event;

public record PosCloseEvent(Long userIdx, Long storeIdx, java.util.Map<Long, Integer> productNeed) {}
