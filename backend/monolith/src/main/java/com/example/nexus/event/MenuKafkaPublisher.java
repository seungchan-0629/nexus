package com.example.nexus.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MenuKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuDomainEvent(MenuDomainEvent e) {
        kafkaTemplate.send(
                e.topic(),
                String.valueOf(e.event().menuIdx()),
                e.event()
        );
    }
}
