package com.iron.service;

import com.iron.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecureNotificationConsumer {

    private final NotificationService notificationService;
    
    private static final Set<String> ALLOWED_NOTIFICATION_ROLES = Set.of(
            "ROLE_NOTIFICATIONS_USER"
    );
    
    private static final Set<String> ALLOWED_INTERNAL_ROLES = Set.of(
            "ROLE_ACCOUNTS_INTERNAL"
    );

    @KafkaListener(
            topics = "${app.kafka.topics.notifications}",
            groupId = "${app.kafka.consumer.group-id}"
    )
    public void consume(NotificationRequest request, Acknowledgment acknowledgment) {
        try {
            if (!hasPermissionToSend(request)) {
                log.warn("Unauthorized notification attempt from service: {} with roles: {} for recipient: {}", 
                        request.getSourceService(), request.getRoles(), request.getRecipientLogin());
                acknowledgment.acknowledge();
                return;
            }
            
            notificationService.save(request);
            log.info("Successfully processed notification from service: {} for recipient: {}", 
                    request.getSourceService(), request.getRecipientLogin());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process notification event for {}", request.getRecipientLogin(), e);
            throw e;
        }
    }
    
    private boolean hasPermissionToSend(NotificationRequest request) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            log.warn("No roles found in notification request from service: {}", request.getSourceService());
            return false;
        }

        boolean hasNotificationRole = request.getRoles().stream()
                .anyMatch(ALLOWED_NOTIFICATION_ROLES::contains);
        
        boolean hasInternalRole = request.getRoles().stream()
                .anyMatch(ALLOWED_INTERNAL_ROLES::contains);
        
        if ("INTERNAL".equals(request.getType())) {
            return hasNotificationRole && hasInternalRole;
        }
        
        return hasNotificationRole;
    }
}
