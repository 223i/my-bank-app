package com.iron.service;

import com.iron.exception.InvalidTransferAmountException;
import com.iron.exception.SelfTransferException;
import com.iron.exception.TransferException;
import com.iron.model.NotificationRequest;
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