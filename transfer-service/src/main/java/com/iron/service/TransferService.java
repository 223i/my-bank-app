package com.iron.service;

import com.iron.exception.TransferException;
import com.iron.dto.NotificationRequest;
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
        try {
            // 1. Списываем у отправителя
            accountsRestClient.patch()
                    .uri("/{login}/decrease-balance?amount={amount}", fromLogin, amount)
                    .retrieve().toBodilessEntity();

            // 2. Начисляем получателю
            accountsRestClient.patch()
                    .uri("/{login}/increase-balance?amount={amount}", toLogin, amount)
                    .retrieve().toBodilessEntity();

            // 3. Уведомляем
            notificationsRestClient.post()
                    .uri("/api/notifications/send")
                    .body(new NotificationRequest(toLogin, "Вам пришел перевод: " + amount, "TRANSFER"))
                    .retrieve().toBodilessEntity();

        } catch ( Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            throw new TransferException("Не удалось выполнить перевод: " + e.getMessage());
        }

    }
}