package com.iron;

import com.iron.dto.NotificationRequest;
import com.iron.service.NotificationConsumer;
import com.iron.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void shouldSaveAndAcknowledgeMessage() {
        NotificationRequest request = new NotificationRequest("user", "msg", "TRANSFER");

        notificationConsumer.consume(request, acknowledgment);

        verify(notificationService).save(request);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeWhenSaveFails() {
        NotificationRequest request = new NotificationRequest("user", "msg", "TRANSFER");
        doThrow(new RuntimeException("DB error")).when(notificationService).save(request);

        assertThatThrownBy(() -> notificationConsumer.consume(request, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");

        verify(notificationService).save(request);
        verify(acknowledgment, never()).acknowledge();
    }
}
