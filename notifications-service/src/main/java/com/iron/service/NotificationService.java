package com.iron.service;

import com.iron.dto.NotificationRequest;
import com.iron.model.Notification;
import com.iron.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final Counter notificationFailuresCounter;

    public NotificationService(NotificationRepository notificationRepository,
                               MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.notificationFailuresCounter = Counter.builder("notification_failures_total")
                .description("Total number of notification failures")
                .tag("service", "notifications-service")
                .register(meterRegistry);
    }

    public void save(NotificationRequest request) {
        try {
            Notification notification = Notification.builder()
                    .recipientLogin(request.getRecipientLogin())
                    .message(request.getMessage())
                    .type(request.getType())
                    .build();

            notificationRepository.save(notification);
            log.info("[NOTIFICATION] To: {} | Type: {} | Message: {}",
                    request.getRecipientLogin(), request.getType(), request.getMessage());
        } catch (Exception e) {
            log.error("Failed to save notification: {}", e.getMessage());
            notificationFailuresCounter.increment();
            throw new RuntimeException("Failed to save notification: " + e.getMessage());
        }
    }
}