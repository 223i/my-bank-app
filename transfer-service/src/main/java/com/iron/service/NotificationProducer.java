package com.iron.service;

import com.iron.model.NotificationRequest;
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
        kafkaTemplate.send(notificationsTopic, request.getRecipientLogin(), request);
        log.info("Notification event sent to Kafka: recipient={}, type={}",
                request.getRecipientLogin(), request.getType());
    }
}
