package com.iron;

import com.iron.dto.NotificationRequest;
import com.iron.model.Notification;
import com.iron.repository.NotificationRepository;
import com.iron.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSaveNotificationEntityFromRequest() {
        NotificationRequest request = NotificationRequest.builder()
                .recipientLogin("user_login")
                .message("Test message")
                .type("TRANSFER")
                .sourceService("test-service")
                .roles(java.util.List.of("ROLE_NOTIFICATIONS_USER"))
                .build();

        notificationService.save(request);

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getRecipientLogin()).isEqualTo("user_login");
        assertThat(savedNotification.getMessage()).isEqualTo("Test message");
        assertThat(savedNotification.getType()).isEqualTo("TRANSFER");
    }
}
