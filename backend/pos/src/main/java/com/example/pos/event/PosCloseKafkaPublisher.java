package com.example.pos.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PosCloseKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPosClose(PosCloseDomainEvent e) {
        log.info("[Kafka Publisher] POS 영업 마감 이벤트 발행 - storeIdx: {}", e.event().storeIdx());
        kafkaTemplate.send(
            KafkaTopics.POS_CLOSE_COMPLETED,
            String.valueOf(e.event().storeIdx()),
            e.event()
        );
    }
}
