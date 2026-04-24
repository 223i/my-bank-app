package com.iron.service;

import com.iron.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${app.kafka.topics.notifications}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(NotificationRequest request, Acknowledgment acknowledgment) {
        try {
            notificationService.save(request);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process notification event for {}", request.getRecipientLogin(), e);
            throw e;
        }
    }


}
