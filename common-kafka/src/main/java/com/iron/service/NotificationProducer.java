package com.iron.service;

import com.iron.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${app.kafka.topics.notifications}")
    private String notificationsTopic;

    public void send(NotificationRequest request) {
        if (request.getSourceService() == null || request.getSourceService().isEmpty()) {
            enrichWithSecurityMetadata(request);
        }
        
        kafkaTemplate.send(notificationsTopic, request.getRecipientLogin(), request)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("Failed to send notification event to Kafka: recipient={}, type={}, source={}, error={}",
                                request.getRecipientLogin(), request.getType(), request.getSourceService(), failure.getMessage(), failure);
                    } else {
                        log.info("Notification event successfully sent to Kafka: recipient={}, type={}, source={}, offset={}",
                                request.getRecipientLogin(), request.getType(), request.getSourceService(), result.getRecordMetadata().offset());
                    }
                });
    }

    private void enrichWithSecurityMetadata(NotificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            request.setSourceService(getServiceName(auth));
            request.setRoles(auth.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .toList());
        } else {
            request.setSourceService("unknown");
            request.setRoles(java.util.List.of());
        }
    }

    private String getServiceName(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            String username = auth.getName();
            if (username.startsWith("service-account-")) {
                return username.replace("service-account-", "");
            }
            return username;
        }
        return "unknown";
    }
}
