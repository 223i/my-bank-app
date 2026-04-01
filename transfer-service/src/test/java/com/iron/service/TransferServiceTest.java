package com.iron.service;

import com.iron.exception.TransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountsRestClient, notificationsRestClient);
    }

    @Test
    @DisplayName("Should throw TransferException when accounts service fails")
    void makeTransfer_throwsWhenAccountsServiceFails() {
        // Mock accounts service to throw exception
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        
        when(accountsRestClient.patch()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("accounts-service unavailable"));

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("accounts-service unavailable");

        verify(accountsRestClient, times(1)).patch();
        verifyNoInteractions(notificationsRestClient);
    }

    @Test
    @DisplayName("Should throw TransferException when notification service fails")
    void makeTransfer_throwsWhenNotificationServiceFails() {
        // Mock first accounts call to succeed
        RestClient.RequestBodyUriSpec decreaseUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec decreaseBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec decreaseResponseSpec = mock(RestClient.ResponseSpec.class);
        
        when(accountsRestClient.patch()).thenReturn(decreaseUriSpec);
        when(decreaseUriSpec.uri(anyString(), any(), any())).thenReturn(decreaseBodySpec);
        when(decreaseBodySpec.retrieve()).thenReturn(decreaseResponseSpec);
        when(decreaseResponseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("First call failed"));

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("First call failed");

        verify(accountsRestClient, times(1)).patch();
        verifyNoInteractions(notificationsRestClient);
    }

    @Test
    @DisplayName("Should handle RestClient exceptions properly")
    void makeTransfer_handlesRestClientExceptions() {
        // Mock accounts service to throw RestClientException
        when(accountsRestClient.patch()).thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("Connection refused");

        verify(accountsRestClient, times(1)).patch();
        verifyNoInteractions(notificationsRestClient);
    }
}
