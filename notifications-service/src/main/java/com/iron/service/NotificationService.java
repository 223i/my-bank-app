package com.iron.service;

import com.iron.dto.NotificationRequest;
import com.iron.model.Notification;
import com.iron.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Counter notificationFailureCounter;

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
            notificationFailureCounter.increment();
            throw new RuntimeException("Failed to save notification: " + e.getMessage());
        }
    }
}
