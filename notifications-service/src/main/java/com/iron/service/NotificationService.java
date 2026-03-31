package com.iron.service;

import com.iron.model.Notification;
import com.iron.dto.NotificationRequest;
import com.iron.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void save(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientLogin(request.getRecipientLogin())
                .message(request.getMessage())
                .type(request.getType())
                .build();

        notificationRepository.save(notification);
        log.info("[NOTIFICATION] To: {} | Type: {} | Message: {}",
                request.getRecipientLogin(), request.getType(), request.getMessage());
    }
}