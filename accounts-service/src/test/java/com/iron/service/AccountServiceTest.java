package com.iron.service;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.mapper.AccountMapper;
import com.iron.model.Account;
import com.iron.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iron.exception.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock
    private AccountMapper mapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountDto testAccountDto;
    private AccountUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .login("testuser")
                .firstName("Test")
                .lastName("User")
                .birthday(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000.50))
                .build();

        testAccountDto = AccountDto.builder()
                .name("Test User")
                .birthday(OffsetDateTime.now())
                .sum(100050L)
                .accounts(List.of())
                .build();

        testUpdateDto = new AccountUpdateDto();
        testUpdateDto.setName("Updated Name");
        testUpdateDto.setBirthday(LocalDate.of(1995, 5, 15));
    }

    @Test
    @DisplayName("Should get account by login successfully")
    void getAccount_Success() {
        String login = "testuser";
        when(accountRepository.findByLogin(login)).thenReturn(Optional.of(testAccount));
        when(mapper.toDto(testAccount)).thenReturn(testAccountDto);

        AccountDto result = accountService.getAccount(login);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        verify(accountRepository).findByLogin(login);
        verify(mapper).toDto(testAccount);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account not found")
    void getAccount_NotFound() {
        String login = "nonexistent";
        when(accountRepository.findByLogin(login)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(login))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found for login: " + login);

        verify(accountRepository).findByLogin(login);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should update account successfully")
    void updateAccount_Success() {
        String login = "testuser";
        Account updatedAccount = Account.builder()
                .id(1L)
                .login("testuser")
                .firstName("Updated")
                .lastName("Name")
                .birthday(LocalDate.of(1995, 5, 15))
                .balance(BigDecimal.valueOf(1000.50))
                .build();

        when(accountRepository.findByLogin(login)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(testAccount)).thenReturn(updatedAccount);
        when(mapper.toDto(updatedAccount)).thenReturn(testAccountDto);

        AccountDto result = accountService.updateAccount(login, testUpdateDto);

        assertThat(result).isNotNull();
        verify(accountRepository).findByLogin(login);
        verify(mapper).updateEntity(testUpdateDto, testAccount);
        verify(accountRepository).save(testAccount);
        verify(mapper).toDto(updatedAccount);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when updating non-existent account")
    void updateAccount_NotFound() {
        String login = "nonexistent";
        when(accountRepository.findByLogin(login)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(login, testUpdateDto))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");

        verify(accountRepository).findByLogin(login);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should search other accounts successfully")
    void searchOthersAccounts_Success() {
        String currentLogin = "testuser";
        List<Account> otherAccounts = List.of(
                Account.builder()
                        .id(2L)
                        .login("otheruser1")
                        .firstName("Other")
                        .lastName("User1")
                        .build(),
                Account.builder()
                        .id(3L)
                        .login("otheruser2")
                        .firstName("Another")
                        .lastName("User2")
                        .build()
        );

        List<AccountPublicDto> expectedDtos = new ArrayList<>();
        AccountPublicDto dto1 = new AccountPublicDto();
        dto1.setLogin("otheruser1");
        dto1.setName("Other User1");
        AccountPublicDto dto2 = new AccountPublicDto();
        dto2.setLogin("otheruser2");
        dto2.setName("Another User2");
        expectedDtos.add(dto1);
        expectedDtos.add(dto2);

        when(accountRepository.findAllByLoginNot(currentLogin)).thenReturn(otherAccounts);
        when(mapper.toPublicDtoList(otherAccounts)).thenReturn(expectedDtos);

        List<AccountPublicDto> result = accountService.searchOthersAccounts(currentLogin);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLogin()).isEqualTo("otheruser1");
        assertThat(result.get(1).getLogin()).isEqualTo("otheruser2");
        verify(accountRepository).findAllByLoginNot(currentLogin);
        verify(mapper).toPublicDtoList(otherAccounts);
    }

    @Test
    @DisplayName("Should return empty list when no other accounts exist")
    void searchOthersAccounts_EmptyList() {
        String currentLogin = "onlyuser";
        when(accountRepository.findAllByLoginNot(currentLogin)).thenReturn(List.of());
        when(mapper.toPublicDtoList(List.of())).thenReturn(List.of());

        List<AccountPublicDto> result = accountService.searchOthersAccounts(currentLogin);

        assertThat(result).isEmpty();
        verify(accountRepository).findAllByLoginNot(currentLogin);
        verify(mapper).toPublicDtoList(List.of());
    }

    @Test
    @DisplayName("Should send notification successfully")
    void sendNotification_Success() {
        String login = "testuser";
        String text = "Test notification";
        String type = "TEST";

        accountService.sendNotification(login, text, type);
    }

    @Test
    @DisplayName("Should handle notification service failure gracefully")
    void sendNotification_Failure() {
        String login = "testuser";
        String text = "Test notification";
        String type = "TEST";

        accountService.sendNotification(login, text, type);
    }
}
