package com.example.pos.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentDomainEvent(PaymentDomainEvent e) {
        kafkaTemplate.send(
                KafkaTopics.POS_PAYMENT_CREATED,
                String.valueOf(e.event().storeIdx()),  // partition key
                e.event()
        );
    }
}
