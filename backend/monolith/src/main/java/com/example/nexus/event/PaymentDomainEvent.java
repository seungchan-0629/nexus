package com.example.nexus.event;

/**
 * 결제 도메인 이벤트.
 *
 * Spring의 ApplicationEventPublisher로 발행되어 트랜잭션 commit 이후
 * PaymentKafkaPublisher가 수신해서 Kafka로 전달한다.
 */
public record PaymentDomainEvent(PaymentEvent event) {}