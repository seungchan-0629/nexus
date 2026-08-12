package com.example.pos.kafka;

import com.example.pos.domain.product.ProductRepository;
import com.example.pos.domain.product.model.Product;
import com.example.pos.event.KafkaTopics;
import com.example.pos.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {
    private final ProductRepository productRepository;

    // monolith에서 발생한 제품 생성 이벤트 구독 -> pos DB 동기화
    @KafkaListener(topics = KafkaTopics.PRODUCT_CREATED, groupId = "pos-kafka-group")
    public void onProductCreatedEvent(ProductEvent event){
        log.info("[Kafka] Pos - Product CreatedEvent 수신: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        try {
            upsert(event);
            log.info("[Kafka] Pos - Product 정보 동기화 완료: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }catch (Exception e) {
            log.info("[Kafka] Pos - Product 정보 동기화 중 에러 발생: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }

    }

    // monolith에서 발생한 제품 수정 이벤트 구독 -> pos DB 동기화
    @KafkaListener(topics = KafkaTopics.PRODUCT_UPDATED, groupId = "pos-kafka-group")
    public void onProductUpdatedEvent(ProductEvent event){
        log.info("[Kafka] Pos - Product UpdatedEvent 수신: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        try {
            upsert(event);
            log.info("[Kafka] Pos - Product 정보 동기화 완료: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }catch (Exception e) {
            log.info("[Kafka] Pos - Product 정보 동기화 중 에러 발생: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }

    }

    // monolith에서 발생한 제품 삭제 이벤트 구독 -> pos DB 동기화
    @KafkaListener(topics = KafkaTopics.PRODUCT_DELETED, groupId = "pos-kafka-group")
    public void onProductDeletedEvent(ProductEvent event){
        log.info("[Kafka] Pos - Product DeletedEvent 수신: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        try {
            upsert(event);
            log.info("[Kafka] Pos - Product 정보 동기화 완료: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }catch (Exception e) {
            log.info("[Kafka] Pos - Product 정보 동기화 중 에러 발생: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }

    }


    private void upsert(ProductEvent event){
        boolean isExist = productRepository.existsById(event.productIdx());

        if (isExist){
            log.info("[Kafka] 기존 제품 정보 수정 및 삭제: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }else{
            log.info("[Kafka] 신규 매장 정보 등록: productIdx={}, categoryName={}", event.productIdx(), event.categoryName());
        }
        Product product = Product.builder()
                .idx(event.productIdx())
                .productName(event.productName())
                .productUnit(event.productUnit())
                .maxStock(event.maxStock())
                .minStock(event.minStock())
                .unitPrice(event.unitPrice())
                .dangerDays(event.dangerDays())
                .isDeleted(event.isDeleted())
                .categoryName(event.categoryName())
                .isNew(!isExist)
                .build();

        productRepository.save(product);
    }
}
