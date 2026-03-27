package com.iron.service;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.dto.NotificationRequest;
import com.iron.mapper.AccountMapper;
import com.iron.model.Account;
import com.iron.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper mapper;
    private final AccountRepository accountRepository;
    private final RestClient notificationsRestClient;

    @SneakyThrows
    public AccountDto getAccount(String login) {
        log.info("Getting account by login: {}", login);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for login: " + login));

        log.debug("Found account ID: {}", account.getId());
        return mapper.toDto(account);
    }

    @Transactional
    @SneakyThrows
    public AccountDto updateAccount(String login, AccountUpdateDto updateDto) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        mapper.updateEntity(updateDto, account);
        sendNotification(
                login,
                "Ваши персональные данные (ФИО/Дата рождения) были успешно изменены.",
                "PROFILE_UPDATE"
        );
        return mapper.toDto(accountRepository.save(account));
    }

    public List<AccountPublicDto> searchOthersAccounts(String currentLogin) {
        log.info("Fetching all accounts except: {}", currentLogin);

        List<Account> others = accountRepository.findAllByLoginNot(currentLogin);

        return mapper.toPublicDtoList(others);
    }

    @Transactional
    @SneakyThrows
    public void decreaseBalance(String login, BigDecimal amount) {
        log.info("Decreasing balance for {} by {}", login, amount);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for login: " + login));
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        sendNotification(login, "С вашего счета списано: " + amount, "BALANCE_DECREASE");
    }

    @Transactional
    @SneakyThrows
    public void increaseBalance(String login, BigDecimal amount) {
        log.info("Increasing balance for {} by {}", login, amount);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for login: " + login));
        
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        sendNotification(login, "На ваш счет зачислено: " + amount, "BALANCE_INCREASE");
    }

    public void sendNotification(String login, String text, String type) {
        NotificationRequest request = NotificationRequest.builder()
                .recipientLogin(login)
                .message(text)
                .type(type)
                .build();

        try {
            notificationsRestClient.post()
                    .uri("/api/notifications") // Эндпоинт в сервисе Notifications
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Notification sent to {} about {}", login, type);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}
