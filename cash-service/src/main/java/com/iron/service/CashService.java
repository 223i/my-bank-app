package com.iron.service;

import com.iron.dto.NotificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {

    private final RestClient accountsRestClient;
    private final NotificationProducer notificationProducer;

    public void processOperation(String login, BigDecimal amount, String type) {
        String uri = type.equalsIgnoreCase("PUT") ? "/increase-balance" : "/decrease-balance";
        String transactionId = UUID.randomUUID().toString();

        try {
            callAccountsService(login, amount, uri, transactionId);

            String message = type.equalsIgnoreCase("PUT")
                    ? "Пополнение на " + amount + " руб. успешно выполнено"
                    : "Снятие " + amount + " руб. успешно выполнено";

            notificationProducer.send(new NotificationRequest(login, message, "CASH_OP"));

        } catch (Exception e) {
            log.error("Cash operation failed: {}", e.getMessage());
            throw new RuntimeException("Не удалось провести операцию: " + e.getMessage());
        }
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "accountsFallback")
    private void callAccountsService(String login, BigDecimal amount, String uri, String transactionId) {
        accountsRestClient.patch()
                .uri("/{login}" + uri + "?amount={amount}&transactionId={transactionId}",
                        login, amount, transactionId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("Ошибка Accounts: " + res.getStatusCode());
                })
                .toBodilessEntity();
    }

    public void accountsFallback(String login, BigDecimal amount, String uri,
                                 String transactionId, Throwable ex) {
        log.error("Accounts service unavailable: transactionId={}", transactionId, ex);
        throw new RuntimeException("Accounts service временно недоступен. Попробуйте повторить операцию позже");
    }
}