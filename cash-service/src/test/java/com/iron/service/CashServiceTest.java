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

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashService Tests")
class CashServiceTest {

    @Mock
    private RestClient accountsRestClient;

    @Mock
    private RestClient notificationsRestClient;

    private CashService cashService;

    private RestClient.RequestBodyUriSpec accountsUriSpec;
    private RestClient.RequestBodySpec accountsBodySpec;
    private RestClient.ResponseSpec accountsResponseSpec;

    private RestClient.RequestBodyUriSpec notifUriSpec;
    private RestClient.RequestBodySpec notifBodySpec;
    private RestClient.ResponseSpec notifResponseSpec;

    @BeforeEach
    void setUp() {
        cashService = new CashService(accountsRestClient, notificationsRestClient);

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
        when(accountsResponseSpec.onStatus(any(), any())).thenReturn(accountsResponseSpec);
        when(accountsResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    private void stubNotificationsPostChain() {
        // lenient() because notification content varies per operation (message text differs)
        // and uri(String, Object...) varargs matching requires lenient mode
        lenient().when(notificationsRestClient.post()).thenReturn(notifUriSpec);
        lenient().when(notifUriSpec.uri(anyString())).thenReturn(notifBodySpec);
        lenient().when(notifBodySpec.body(any())).thenReturn(notifBodySpec);
        lenient().when(notifBodySpec.retrieve()).thenReturn(notifResponseSpec);
        lenient().when(notifResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    @DisplayName("Should deposit (PUT) successfully")
    void processOperation_putSuccess() {
        stubAccountsPatchChain();
        stubNotificationsPostChain();

        assertThatNoException().isThrownBy(() ->
                cashService.processOperation("jdoe", BigDecimal.valueOf(500), "PUT"));

        verify(accountsRestClient).patch();
        verify(notificationsRestClient).post();
    }

    @Test
    @DisplayName("Should withdraw (GET) successfully")
    void processOperation_getSuccess() {
        stubAccountsPatchChain();
        stubNotificationsPostChain();

        assertThatNoException().isThrownBy(() ->
                cashService.processOperation("jdoe", BigDecimal.valueOf(200), "GET"));

        verify(accountsRestClient).patch();
        verify(notificationsRestClient).post();
    }

    @Test
    @DisplayName("Should throw RuntimeException when accounts service fails on deposit")
    void processOperation_throwsWhenAccountsServiceFails() {
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
    @DisplayName("PUT uses increase-balance URI, GET uses decrease-balance URI")
    void processOperation_usesCorrectUri() {
        stubAccountsPatchChain();
        stubNotificationsPostChain();

        cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT");
        cashService.processOperation("jdoe", BigDecimal.valueOf(50), "GET");

        verify(accountsUriSpec).uri(contains("increase-balance"), any(), any());
        verify(accountsUriSpec).uri(contains("decrease-balance"), any(), any());
    }
}