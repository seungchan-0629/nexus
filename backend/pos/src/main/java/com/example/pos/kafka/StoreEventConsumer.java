package com.example.pos.kafka;

import com.example.pos.domain.store.StoreRepository;
import com.example.pos.domain.store.model.Store;
import com.example.pos.event.KafkaTopics;
import com.example.pos.event.StoreEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEventConsumer {
    private final StoreRepository storeRepository;


    // monolith 에서 발생한 매장 생성 이벤트 구독 -> pos DB 동기화
    @KafkaListener(topics = KafkaTopics.STORE_CREATED, groupId = "pos-kafka-group")
    public void onStoreCreatedEvnet(StoreEvent event){
        log.info("[Kafka] Pos - Store CreateEvent 수신: storeIdx={}, storeName={}", event.storeIdx(), event.storeName());
        try{
            // 이미 있는 매장인지 확인
            upsert(event);
            log.info("[Kafka] Pos - Store 정보 동기화 완료: {}", event.storeName());
        }catch (Exception e) {
            log.error("[Kafka] Pos - Store 동기화 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    // monolith 에서 발생한 매장 수정 이벤트 구독 -> pos DB 동기화
    @KafkaListener(topics = KafkaTopics.STORE_UPDATED, groupId = "pos-kafka-group")
    public void onStoreUpdatedEvnet(StoreEvent event){
        log.info("[Kafka] Pos - Store DeleteEvent 수신: storeIdx={}, storeName={}", event.storeIdx(), event.storeName(), event.isDeleted());
        try{
            // 이미 있는 매장인지 확인
            upsert(event);
            log.info("[Kafka] Pos - Store 정보 동기화 완료: {}", event.storeName());
        }catch (Exception e) {
            log.error("[Kafka] Pos - Store 동기화 중 에러 발생: {}", e.getMessage(), e);
        }
    }


    private void upsert(StoreEvent event){
        boolean isExist = storeRepository.existsById(event.storeIdx());

        if (isExist) {
            log.info("[Kafka] 기존 매장 정보 업데이트: storeIdx={}", event.storeIdx());
        }else{
            log.info("[Kafka] 신규 매장 정보 등록: storeIdx={}", event.storeIdx());
        }
        Store store = Store.builder()
                .idx(event.storeIdx())
                .storeName(event.storeName())
                .address(event.address())
                .addressDetail(event.addressDetail())
                .isDeleted(event.isDeleted())
                .userIdx(event.userIdx())
                .isNew(!isExist)
                .build();

        storeRepository.save(store);
    }
}
