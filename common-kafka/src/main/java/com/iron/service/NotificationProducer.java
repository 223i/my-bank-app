package com.iron.service;

import com.iron.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${app.kafka.topics.notifications}")
    private String notificationsTopic;

    public void send(NotificationRequest request) {
        kafkaTemplate.send(notificationsTopic, request.getRecipientLogin(), request)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("Failed to send notification event to Kafka: recipient={}, type={}, error={}",
                                request.getRecipientLogin(), request.getType(), failure.getMessage(), failure);
                    } else {
                        log.info("Notification event successfully sent to Kafka: recipient={}, type={}, offset={}",
                                request.getRecipientLogin(), request.getType(), result.getRecordMetadata().offset());
                    }
                });
    }
}
