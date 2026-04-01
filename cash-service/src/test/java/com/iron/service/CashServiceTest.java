package com.iron.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashService Tests")
class CashServiceTest {

    @Mock
    private RestClient accountsRestClient;

    @Mock
    private RestClient notificationsRestClient;

    private CashService cashService;

    @BeforeEach
    void setUp() {
        cashService = new CashService(accountsRestClient, notificationsRestClient);
    }

    @Test
    @DisplayName("Should throw RuntimeException when accounts service fails on deposit")
    void processOperation_throwsWhenAccountsServiceFails() {
        // Mock accounts service chain to throw exception
        RestClient.RequestBodyUriSpec accountsUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec accountsBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec accountsResponseSpec = mock(RestClient.ResponseSpec.class);

        when(accountsRestClient.patch()).thenReturn(accountsUriSpec);
        when(accountsUriSpec.uri(anyString(), any(), any())).thenReturn(accountsBodySpec);
        when(accountsBodySpec.retrieve()).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.onStatus(any(), any())).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("accounts-service unavailable"));

        assertThatThrownBy(() -> cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("accounts-service unavailable");

        verifyNoInteractions(notificationsRestClient);
    }

    @Test
    @DisplayName("Should call accounts service with correct URI for PUT operation")
    void processOperation_callsAccountsWithCorrectUriForPut() {
        // Mock accounts service chain
        RestClient.RequestBodyUriSpec accountsUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec accountsBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec accountsResponseSpec = mock(RestClient.ResponseSpec.class);

        when(accountsRestClient.patch()).thenReturn(accountsUriSpec);
        when(accountsUriSpec.uri(anyString(), any(), any())).thenReturn(accountsBodySpec);
        when(accountsBodySpec.retrieve()).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.onStatus(any(), any())).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        // Mock notifications service chain
        RestClient.RequestBodyUriSpec notifUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec notifBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec notifResponseSpec = mock(RestClient.ResponseSpec.class);

        when(notificationsRestClient.post()).thenReturn(notifUriSpec);
        when(notifUriSpec.uri(anyString())).thenReturn(notifBodySpec);
        when(notifBodySpec.body(any())).thenReturn(notifBodySpec);
        when(notifBodySpec.retrieve()).thenReturn(notifResponseSpec);
        when(notifResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        try {
            cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT");
        } catch (Exception e) {
            // Ignore any exceptions, we just want to verify the URI was called correctly
        }

        verify(accountsUriSpec).uri("/{login}/increase-balance?amount={amount}", "jdoe", BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("Should call accounts service with correct URI for GET operation")
    void processOperation_callsAccountsWithCorrectUriForGet() {
        // Mock accounts service chain
        RestClient.RequestBodyUriSpec accountsUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec accountsBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec accountsResponseSpec = mock(RestClient.ResponseSpec.class);

        when(accountsRestClient.patch()).thenReturn(accountsUriSpec);
        when(accountsUriSpec.uri(anyString(), any(), any())).thenReturn(accountsBodySpec);
        when(accountsBodySpec.retrieve()).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.onStatus(any(), any())).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        // Mock notifications service chain
        RestClient.RequestBodyUriSpec notifUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec notifBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec notifResponseSpec = mock(RestClient.ResponseSpec.class);

        when(notificationsRestClient.post()).thenReturn(notifUriSpec);
        when(notifUriSpec.uri(anyString())).thenReturn(notifBodySpec);
        when(notifBodySpec.body(any())).thenReturn(notifBodySpec);
        when(notifBodySpec.retrieve()).thenReturn(notifResponseSpec);
        when(notifResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        try {
            cashService.processOperation("jdoe", BigDecimal.valueOf(50), "GET");
        } catch (Exception e) {
            // Ignoring any exceptions, just want to verify the URI was called correctly
        }

        verify(accountsUriSpec).uri("/{login}/decrease-balance?amount={amount}", "jdoe", BigDecimal.valueOf(50));
    }
}
