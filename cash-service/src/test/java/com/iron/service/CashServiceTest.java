package com.iron.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    
    private RestClient.RequestBodyUriSpec mockAccountsChainSuccess() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(accountsRestClient.patch()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
        return uriSpec;
    }

    private void mockNotificationsChainSuccess() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(notificationsRestClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.body(any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    @DisplayName("Should call increase-balance URI for PUT operation")
    void processOperation_callsIncreaseBalanceForPut() {
        RestClient.RequestBodyUriSpec uriSpec = mockAccountsChainSuccess();
        mockNotificationsChainSuccess();

        cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT");

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(uriSpec).uri(uriCaptor.capture(), any(), any(), any());
        assertThat(uriCaptor.getValue()).contains("increase-balance");
    }

    @Test
    @DisplayName("Should call decrease-balance URI for GET operation")
    void processOperation_callsDecreaseBalanceForGet() {
        RestClient.RequestBodyUriSpec uriSpec = mockAccountsChainSuccess();
        mockNotificationsChainSuccess();

        cashService.processOperation("jdoe", BigDecimal.valueOf(50), "GET");

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(uriSpec).uri(uriCaptor.capture(), any(), any(), any());
        assertThat(uriCaptor.getValue()).contains("decrease-balance");
    }

    @Test
    @DisplayName("Should pass transactionId as 3rd URI variable")
    void processOperation_passesTransactionIdInUri() {
        RestClient.RequestBodyUriSpec uriSpec = mockAccountsChainSuccess();
        mockNotificationsChainSuccess();

        cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT");

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> arg3Captor = ArgumentCaptor.forClass(Object.class);
        verify(uriSpec).uri(uriCaptor.capture(), any(), any(), arg3Captor.capture());

        assertThat(uriCaptor.getValue()).contains("transactionId={transactionId}");
        assertThat(arg3Captor.getValue()).isNotNull();
        assertThat(arg3Captor.getValue().toString()).isNotBlank();
    }

    @Test
    @DisplayName("Should generate a unique transactionId per operation")
    void processOperation_generatesUniqueTransactionIdPerCall() {
        RestClient.RequestBodyUriSpec uriSpec = mockAccountsChainSuccess();
        mockNotificationsChainSuccess();

        cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT");
        cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT");

        ArgumentCaptor<Object> arg3Captor = ArgumentCaptor.forClass(Object.class);
        verify(uriSpec, times(2)).uri(anyString(), any(), any(), arg3Captor.capture());

        List<Object> txIds = arg3Captor.getAllValues();
        assertThat(txIds.get(0).toString()).isNotEqualTo(txIds.get(1).toString());
    }

    @Test
    @DisplayName("Should throw RuntimeException when accounts service fails on deposit")
    void processOperation_throwsWhenAccountsServiceFails() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(accountsRestClient.patch()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("accounts-service unavailable"));

        assertThatThrownBy(() -> cashService.processOperation("jdoe", BigDecimal.valueOf(100), "PUT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("accounts-service unavailable");

        verifyNoInteractions(notificationsRestClient);
    }
}