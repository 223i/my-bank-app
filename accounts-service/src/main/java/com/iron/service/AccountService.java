package com.iron.service;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.dto.NotificationRequest;
import com.iron.exception.AccountNotFoundException;
import com.iron.mapper.AccountMapper;
import com.iron.model.Account;
import com.iron.model.ProcessedTransaction;
import com.iron.repository.AccountRepository;
import com.iron.repository.ProcessedTransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableMethodSecurity
public class AccountService {

    private static final int MIN_AGE = 18;

    private final AccountMapper mapper;
    private final AccountRepository accountRepository;
    private final RestClient notificationsRestClient;
    private final ProcessedTransactionRepository processedTransactionRepository;

    public AccountDto getAccount(String login) {
        log.info("Getting account by login: {}", login);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for login: " + login));

        // Включаем список других аккаунтов для отображения dropdown переводов
        AccountDto base = mapper.toDto(account);
        List<AccountDto> otherAccounts = accountRepository.findAllByLoginNot(login).stream()
                .map(mapper::toDto)
                .toList();

        return AccountDto.builder()
                .login(base.getLogin())
                .name(base.getName())
                .birthday(base.getBirthday())
                .sum(base.getSum())
                .accounts(otherAccounts)
                .build();
    }

    @Transactional
    public AccountDto updateAccount(String login, AccountUpdateDto updateDto) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        validateAge(updateDto.getBirthday());

        mapper.updateEntity(updateDto, account);
        account = accountRepository.save(account);

        sendNotification(login,
                "Ваши персональные данные (ФИО/Дата рождения) были успешно изменены.",
                "PROFILE_UPDATE");

        return mapper.toDto(account);
    }

    public List<AccountPublicDto> searchOthersAccounts(String currentLogin) {
        log.info("Fetching all accounts except: {}", currentLogin);
        return mapper.toPublicDtoList(accountRepository.findAllByLoginNot(currentLogin));
    }

    @Transactional
    public void decreaseBalance(String login, BigDecimal amount, String transactionId) {
        if (processedTransactionRepository.existsById(transactionId)) {
            log.info("Transaction {} already processed (decrease), skipping", transactionId);
            return;
        }
        log.info("Decreasing balance for {} by {}", login, amount);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for login: " + login));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds: balance " + account.getBalance() + " < " + amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        processedTransactionRepository.save(new ProcessedTransaction(transactionId));

        sendNotification(login, "С вашего счета списано: " + amount + " руб.", "BALANCE_DECREASE");
    }

    @Transactional
    public void increaseBalance(String login, BigDecimal amount, String transactionId) {
        if (processedTransactionRepository.existsById(transactionId)) {
            log.info("Transaction {} already processed (increase), skipping", transactionId);
            return;
        }
        log.info("Increasing balance for {} by {}", login, amount);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for login: " + login));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        processedTransactionRepository.save(new ProcessedTransaction(transactionId));

        sendNotification(login, "На ваш счет зачислено: " + amount + " руб.", "BALANCE_INCREASE");
    }

    public void sendNotification(String login, String text, String type) {
        NotificationRequest request = NotificationRequest.builder()
                .recipientLogin(login)
                .message(text)
                .type(type)
                .build();
        try {
            sendNotification(login, type, request);
        } catch (Exception e) {
            // Не прерываем основную операцию из-за ошибки уведомления
            log.error("Failed to send notification to {}: {}", login, e.getMessage());
        }
    }

    @Retry(name = "notificationsService")
    @CircuitBreaker(name = "notificationsService", fallbackMethod = "notificationsFallback")
    private void sendNotification(String login, String type, NotificationRequest request) {
        notificationsRestClient.post()
                .uri("/api/notifications/send")
                .body(request)
                .retrieve()
                .toBodilessEntity();
        log.info("Notification sent to {} about {}", login, type);
    }

    public void notificationsFallback(Throwable ex) {
        log.error("Notifications service unavailable: {}", ex.getMessage());
        throw new RuntimeException("Notifications service временно недоступен. Попробуйте повторить операцию позже");
    }

    private void validateAge(LocalDate birthday) {
        if (birthday != null && Period.between(birthday, LocalDate.now()).getYears() < MIN_AGE) {
            throw new IllegalArgumentException("Account holder must be at least " + MIN_AGE + " years old");
        }
    }
}