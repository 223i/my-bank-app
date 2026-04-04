package com.iron.service;

import com.iron.exception.InvalidTransferAmountException;
import com.iron.exception.SelfTransferException;
import com.iron.exception.TransferException;
import com.iron.model.NotificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestClient accountsRestClient;
    private final RestClient notificationsRestClient;

    public void makeTransfer(String fromLogin, String toLogin, BigDecimal amount) {
        log.info("Transfer from {} to {} for amount {}", fromLogin, toLogin, amount);
        if (fromLogin.equals(toLogin)) { throw new SelfTransferException("Cannot transfer to the same account"); }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) { throw new InvalidTransferAmountException("Amount must be positive"); }

        try {
            changeBalance(fromLogin, toLogin, amount);
            sendNotification(toLogin, amount);
        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            throw new TransferException("Не удалось выполнить перевод: " + e.getMessage());
        }
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "accountsFallback")
    public void changeBalance(String fromLogin, String toLogin, BigDecimal amount) {
        try {
            // 1. списание
            accountsRestClient.patch()
                    .uri("/{login}/decrease-balance?amount={amount}", fromLogin, amount)
                    .retrieve()
                    .toBodilessEntity();

            // 2. начисление
            accountsRestClient.patch()
                    .uri("/{login}/increase-balance?amount={amount}", toLogin, amount)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            log.error("Error during balance change, starting rollback", e);

            try {
                accountsRestClient.patch()
                        .uri("/{login}/increase-balance?amount={amount}", fromLogin, amount)
                        .retrieve()
                        .toBodilessEntity();

            } catch (Exception rollbackEx) {
                log.error("CRITICAL: rollback failed!", rollbackEx);
            }
            throw e;
        }
    }

    public void accountsFallback(Throwable ex) {
        log.error("Accounts service unavailable", ex);
        throw new TransferException("Accounts service временно недоступен");
    }

    @Retry(name = "notificationsService")
    @CircuitBreaker(name = "notificationsService", fallbackMethod = "notificationsFallback")
    public void sendNotification(String toLogin, BigDecimal amount) {

        notificationsRestClient.post()
                .uri("/api/notifications/send")
                .body(new NotificationRequest(
                        toLogin,
                        "Вам пришел перевод: " + amount,
                        "TRANSFER"))
                .retrieve()
                .toBodilessEntity();
    }

    public void notificationsFallback(Throwable ex) {
        log.error("Notification failed, but transfer completed", ex);
    }
}