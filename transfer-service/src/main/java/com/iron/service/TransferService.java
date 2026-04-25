package com.iron.service;

import com.iron.exception.InvalidTransferAmountException;
import com.iron.exception.SelfTransferException;
import com.iron.exception.TransferException;
import com.iron.dto.NotificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestClient accountsRestClient;
    private final NotificationProducer notificationProducer;

    public void makeTransfer(String fromLogin, String toLogin, BigDecimal amount) {
        log.info("Transfer from {} to {} for amount {}", fromLogin, toLogin, amount);
        if (fromLogin.equals(toLogin)) {
            throw new SelfTransferException("Cannot transfer to the same account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferAmountException("Amount must be positive");
        }

        String transactionId = UUID.randomUUID().toString();
        try {
            changeBalance(fromLogin, toLogin, amount, transactionId);
            notificationProducer.send(NotificationRequest.builder()
                    .recipientLogin(toLogin)
                    .message("Вам пришел перевод: " + amount)
                    .type("TRANSFER")
                    .sourceService("transfer-service")
                    .roles(java.util.List.of("ROLE_NOTIFICATIONS_USER", "ROLE_ACCOUNTS_INTERNAL"))
                    .build());

        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            throw new TransferException("Не удалось выполнить перевод: " + e.getMessage());
        }
    }

    // Обе операции идемпотентны: повторный retry не вызовет двойного списания/зачисления,
    // так как accounts-service проверяет transactionId перед выполнением операции.
    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "accountsFallback")
    public void changeBalance(String fromLogin, String toLogin, BigDecimal amount, String transactionId) {
        accountsRestClient.patch()
                .uri("/{login}/decrease-balance?amount={amount}&transactionId={transactionId}",
                        fromLogin, amount, transactionId + "-debit")
                .retrieve()
                .toBodilessEntity();

        accountsRestClient.patch()
                .uri("/{login}/increase-balance?amount={amount}&transactionId={transactionId}",
                        toLogin, amount, transactionId + "-credit")
                .retrieve()
                .toBodilessEntity();
    }

    public void accountsFallback(String fromLogin, String toLogin, BigDecimal amount,
                                 String transactionId, Throwable ex) {
        log.error("Accounts service unavailable after retries, transactionId={}", transactionId, ex);
        throw new TransferException("Accounts service временно недоступен");
    }
}