package com.iron.service;

import com.iron.exception.TransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Tests")
class TransferServiceTest {

    @Mock
    private RestClient accountsRestClient;

    @Mock
    private RestClient notificationsRestClient;

    private TransferService transferService;

    // Shared mocks for fluent RestClient chain
    private RestClient.RequestBodyUriSpec accountsUriSpec;
    private RestClient.RequestBodySpec accountsBodySpec;
    private RestClient.ResponseSpec accountsResponseSpec;

    private RestClient.RequestBodyUriSpec notifUriSpec;
    private RestClient.RequestBodySpec notifBodySpec;
    private RestClient.ResponseSpec notifResponseSpec;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountsRestClient, notificationsRestClient);

        accountsUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        accountsBodySpec = mock(RestClient.RequestBodySpec.class);
        accountsResponseSpec = mock(RestClient.ResponseSpec.class);

        notifUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        notifBodySpec = mock(RestClient.RequestBodySpec.class);
        notifResponseSpec = mock(RestClient.ResponseSpec.class);
    }

    private void stubAccountsPatchChain() {
        when(accountsRestClient.patch()).thenReturn(accountsUriSpec);
        when(accountsUriSpec.uri(anyString(), any(), any())).thenReturn(accountsBodySpec);
        when(accountsBodySpec.retrieve()).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    private void stubNotificationsPostChain() {
        // lenient() because uri(String, Object...) varargs strict-stubbing mismatch
        lenient().when(notificationsRestClient.post()).thenReturn(notifUriSpec);
        lenient().when(notifUriSpec.uri(anyString())).thenReturn(notifBodySpec);
        lenient().when(notifBodySpec.body(any())).thenReturn(notifBodySpec);
        lenient().when(notifBodySpec.retrieve()).thenReturn(notifResponseSpec);
        lenient().when(notifResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    @DisplayName("Should complete transfer successfully")
    void makeTransfer_success() {
        stubAccountsPatchChain();
        stubNotificationsPostChain();

        assertThatNoException().isThrownBy(() ->
                transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(500)));

        verify(accountsRestClient, times(2)).patch();
        verify(notificationsRestClient).post();
    }

    @Test
    @DisplayName("Should throw TransferException when decrease balance call fails")
    void makeTransfer_throwsWhenDecreaseBalanceFails() {
        when(accountsRestClient.patch()).thenReturn(accountsUriSpec);
        when(accountsUriSpec.uri(anyString(), any(), any())).thenReturn(accountsBodySpec);
        when(accountsBodySpec.retrieve()).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("accounts-service unavailable"));

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("accounts-service unavailable");

        verify(accountsRestClient, times(1)).patch();
        verifyNoInteractions(notificationsRestClient);
    }

    @Test
    @DisplayName("Should throw TransferException when increase balance call fails")
    void makeTransfer_throwsWhenIncreaseBalanceFails() {
        when(accountsRestClient.patch()).thenReturn(accountsUriSpec);
        when(accountsUriSpec.uri(anyString(), any(), any())).thenReturn(accountsBodySpec);
        when(accountsBodySpec.retrieve()).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity())
                .thenReturn(ResponseEntity.ok().build())          // decrease succeeds
                .thenThrow(new RestClientException("Receiver not found")); // increase fails

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("Receiver not found");

        verify(accountsRestClient, times(2)).patch();
        verifyNoInteractions(notificationsRestClient);
    }

    @Test
    @DisplayName("Notification failure should not block successful transfer")
    void makeTransfer_notificationFailureIsWrappedInException() {
        stubAccountsPatchChain();

        when(notificationsRestClient.post()).thenReturn(notifUriSpec);
        when(notifUriSpec.uri(anyString())).thenReturn(notifBodySpec);
        when(notifBodySpec.body(any())).thenReturn(notifBodySpec);
        when(notifBodySpec.retrieve()).thenReturn(notifResponseSpec);
        when(notifResponseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("notifications-service unavailable"));

        // TransferService wraps all exceptions in TransferException — notification failure propagates
        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class);
    }
}