package com.iron.service;

import com.iron.exception.InvalidTransferAmountException;
import com.iron.exception.SelfTransferException;
import com.iron.exception.TransferException;
import com.iron.dto.NotificationRequest;
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
@DisplayName("TransferService Tests")
class TransferServiceTest {

    @Mock
    private RestClient accountsRestClient;

    @Mock
    private NotificationProducer notificationProducer;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountsRestClient, notificationProducer);
    }

    private RestClient.RequestBodyUriSpec mockAccountsChainSuccess() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(accountsRestClient.patch()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        return uriSpec;
    }

    @Test
    @DisplayName("Should call decrease then increase with -debit / -credit transactionId suffixes")
    void makeTransfer_passesTransactionIdWithCorrectSuffixes() {
        RestClient.RequestBodyUriSpec uriSpec = mockAccountsChainSuccess();

        transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100));

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> arg3Captor = ArgumentCaptor.forClass(Object.class);
        verify(uriSpec, times(2)).uri(uriCaptor.capture(), any(), any(), arg3Captor.capture());

        List<String> uris = uriCaptor.getAllValues();
        List<Object> txIds = arg3Captor.getAllValues();

        assertThat(uris.get(0)).contains("decrease-balance");
        assertThat(uris.get(1)).contains("increase-balance");
        assertThat(txIds.get(0).toString()).endsWith("-debit");
        assertThat(txIds.get(1).toString()).endsWith("-credit");
        verify(notificationProducer, times(1)).send(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("Should generate a unique transactionId per transfer")
    void makeTransfer_generatesUniqueTransactionIdPerCall() {
        RestClient.RequestBodyUriSpec uriSpec = mockAccountsChainSuccess();

        transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(50));
        transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(50));

        ArgumentCaptor<Object> arg3Captor = ArgumentCaptor.forClass(Object.class);
        verify(uriSpec, times(4)).uri(anyString(), any(), any(), arg3Captor.capture());

        List<Object> txIds = arg3Captor.getAllValues();
        String firstBase = txIds.get(0).toString().replace("-debit", "");
        String secondBase = txIds.get(2).toString().replace("-debit", "");
        assertThat(firstBase).isNotEqualTo(secondBase);
        verify(notificationProducer, times(2)).send(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("Should send notification to recipient after successful transfer")
    void makeTransfer_sendsKafkaNotification() {
        mockAccountsChainSuccess();

        transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100));

        ArgumentCaptor<NotificationRequest> notificationCaptor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationProducer).send(notificationCaptor.capture());

        NotificationRequest notification = notificationCaptor.getValue();
        assertThat(notification.getRecipientLogin()).isEqualTo("alice_99");
        assertThat(notification.getType()).isEqualTo("TRANSFER");
        assertThat(notification.getMessage()).contains("100");
    }


    @Test
    @DisplayName("Should throw TransferException when accounts service fails (no rollback)")
    void makeTransfer_throwsWhenAccountsServiceFails() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(accountsRestClient.patch()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(), any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new RestClientException("accounts-service unavailable"));

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("accounts-service unavailable");

        verify(accountsRestClient, times(1)).patch();
        verifyNoInteractions(notificationProducer);
    }

    @Test
    @DisplayName("Should throw TransferException when connection refused (no rollback)")
    void makeTransfer_handlesRestClientExceptions() {
        when(accountsRestClient.patch()).thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("Connection refused");

        verify(accountsRestClient, times(1)).patch();
        verifyNoInteractions(notificationProducer);
    }

    @Test
    @DisplayName("Should throw SelfTransferException when transferring to same account")
    void makeTransfer_throwsSelfTransferException() {
        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "jdoe", BigDecimal.valueOf(100)))
                .isInstanceOf(SelfTransferException.class)
                .hasMessage("Cannot transfer to the same account")
                .extracting("errorCode")
                .isEqualTo("TRANSFER_ERROR");

        verifyNoInteractions(accountsRestClient);
        verifyNoInteractions(notificationProducer);
    }

    @Test
    @DisplayName("Should throw InvalidTransferAmountException when amount is zero")
    void makeTransfer_throwsInvalidTransferAmountExceptionWhenAmountIsZero() {
        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.ZERO))
                .isInstanceOf(InvalidTransferAmountException.class)
                .hasMessage("Amount must be positive")
                .extracting("errorCode")
                .isEqualTo("TRANSFER_ERROR");

        verifyNoInteractions(accountsRestClient);
        verifyNoInteractions(notificationProducer);
    }

    @Test
    @DisplayName("Should throw InvalidTransferAmountException when amount is negative")
    void makeTransfer_throwsInvalidTransferAmountExceptionWhenAmountIsNegative() {
        assertThatThrownBy(() -> transferService.makeTransfer("jdoe", "alice_99", BigDecimal.valueOf(-50)))
                .isInstanceOf(InvalidTransferAmountException.class)
                .hasMessage("Amount must be positive")
                .extracting("errorCode")
                .isEqualTo("TRANSFER_ERROR");

        verifyNoInteractions(accountsRestClient);
        verifyNoInteractions(notificationProducer);
    }

    @Test
    @DisplayName("Should throw SelfTransferException with custom error code")
    void makeTransfer_throwsSelfTransferExceptionWithCustomErrorCode() {
        assertThatThrownBy(() -> transferService.makeTransfer("alice_99", "alice_99", BigDecimal.valueOf(100)))
                .isInstanceOf(SelfTransferException.class)
                .hasMessage("Cannot transfer to the same account")
                .extracting("errorCode")
                .isEqualTo("TRANSFER_ERROR");

        verifyNoInteractions(accountsRestClient);
        verifyNoInteractions(notificationProducer);
    }
}