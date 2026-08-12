package com.example.nexus.event;

/**
 * 매장 도메인 이벤트.
 *
 * topic 으로 created/updated 같은 하위 이벤트를 구분한다.
 * 값은 KafkaTopics.STORE_CREATED, KafkaTopics.STORE_UPDATED 중 하나.
 */
public record StoreDomainEvent(StoreEvent event, String topic) {}