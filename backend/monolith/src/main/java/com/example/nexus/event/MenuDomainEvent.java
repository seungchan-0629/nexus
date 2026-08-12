package com.example.nexus.event;

/**
 * 메뉴 도메인 이벤트.
 *
 * topic 으로 created/updated/deleted 같은 하위 이벤트를 구분한다.
 * 값은 KafkaTopics.MENU_CREATED, KafkaTopics.MENU_UPDATED, KafkaTopics.MENU_DELETED 중 하나.
 */
public record MenuDomainEvent(MenuEvent event, String topic) {}