package com.iron.service;

import com.iron.model.NotificationRequest;
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
        String uri = type.equalsIgnoreCase("DEPOSIT") ? "/increase-balance" : "/decrease-balance";

        try {
            // 1. Запрос в Accounts
            accountsRestClient.patch()
                    .uri("/{login}" + uri + "?amount={amount}", login, amount)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RuntimeException("Ошибка Accounts: " + res.getStatusCode());
                    })
                    .toBodilessEntity();

            // 2. Уведомление
            String msg = type.equalsIgnoreCase("DEPOSIT") ? "Пополнение на " : "Снятие ";
            notificationsRestClient.post()
                    .uri("/send")
                    .body(new NotificationRequest(login, msg + amount + " руб. успешно выполнено", "CASH_OP"))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            log.error("Cash operation failed: {}", e.getMessage());
            throw new RuntimeException("Не удалось провести операцию: " + e.getMessage());
        }
    }
}
