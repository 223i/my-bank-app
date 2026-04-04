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

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {

    private final RestClient accountsRestClient;
    private final RestClient notificationsRestClient;

    public void processOperation(String login, BigDecimal amount, String type) {
        String uri = type.equalsIgnoreCase("PUT") ? "/increase-balance" : "/decrease-balance";

        try {
            // 1. Запрос в Accounts
            callAccountsService(login, amount, uri);

            // 2. Уведомление
            String msg = type.equalsIgnoreCase("PUT") ? "Пополнение на " : "Снятие ";
            callNotificationsService(login, amount, msg);

        } catch (Exception e) {
            log.error("Cash operation failed: {}", e.getMessage());
            throw new RuntimeException("Не удалось провести операцию: " + e.getMessage());
        }
    }

    @Retry(name = "notificationsService")
    @CircuitBreaker(name = "notificationsService", fallbackMethod = "notificationsFallback")
    private void callNotificationsService(String login, BigDecimal amount, String msg) {
        notificationsRestClient.post()
                .uri("/api/notifications/send")
                .body(new NotificationRequest(login, msg + amount + " руб. успешно выполнено", "CASH_OP"))
                .retrieve()
                .toBodilessEntity();
    }


    @CircuitBreaker(name = "accountsService", fallbackMethod = "accountsFallback")
    private void callAccountsService(String login, BigDecimal amount, String uri) {
        accountsRestClient.patch()
                .uri("/{login}" + uri + "?amount={amount}", login, amount)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("Ошибка Accounts: " + res.getStatusCode());
                })
                .toBodilessEntity();
    }

    public void accountsFallback(Throwable ex) {
        log.error("Accounts service unavailable: {}", ex.getMessage());
        throw new RuntimeException("Accounts service временно недоступен. Попробуйте повторить операцию позже");
    }

    public void notificationsFallback(Throwable ex) {
        log.error("Notifications service unavailable: {}", ex.getMessage());
        throw new RuntimeException("Notifications service временно недоступен. Попробуйте повторить операцию позже");
    }
}
