package com.iron.testutils;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.model.Account;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Account createTestAccount(String login) {
        return Account.builder()
                .id(1L)
                .login(login)
                .firstName("Test")
                .lastName("User")
                .birthday(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000.50))
                .build();
    }

    public static Account createTestAccount(String login, String firstName, String lastName, 
                                          LocalDate birthday, BigDecimal balance) {
        return Account.builder()
                .id(1L)
                .login(login)
                .firstName(firstName)
                .lastName(lastName)
                .birthday(birthday)
                .balance(balance)
                .build();
    }

    public static AccountDto createTestAccountDto() {
        return AccountDto.builder()
                .name("Test User")
                .birthday(OffsetDateTime.now())
                .sum(100050L)
                .accounts(List.of())
                .build();
    }

    public static AccountDto createTestAccountDto(String name, Long sum) {
        return AccountDto.builder()
                .name(name)
                .birthday(OffsetDateTime.now())
                .sum(sum)
                .accounts(List.of())
                .build();
    }

    public static AccountUpdateDto createTestAccountUpdateDto() {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setName("Updated Name");
        dto.setBirthday(LocalDate.of(1995, 5, 15));
        return dto;
    }

    public static AccountUpdateDto createTestAccountUpdateDto(String name, LocalDate birthday) {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setName(name);
        dto.setBirthday(birthday);
        return dto;
    }

    public static AccountPublicDto createTestAccountPublicDto(String login, String name) {
        AccountPublicDto dto = new AccountPublicDto();
        dto.setLogin(login);
        dto.setName(name);
        return dto;
    }

    public static List<Account> createTestAccountList() {
        return List.of(
                createTestAccount("user1", "First", "User", LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000.00)),
                createTestAccount("user2", "Second", "User", LocalDate.of(1985, 5, 15), BigDecimal.valueOf(500.50)),
                createTestAccount("user3", "Third", "User", LocalDate.of(1995, 8, 20), BigDecimal.valueOf(2000.75))
        );
    }

    public static List<AccountPublicDto> createTestAccountPublicDtoList() {
        List<AccountPublicDto> dtos = new ArrayList<>();
        dtos.add(createTestAccountPublicDto("user1", "First User"));
        dtos.add(createTestAccountPublicDto("user2", "Second User"));
        dtos.add(createTestAccountPublicDto("user3", "Third User"));
        return dtos;
    }

    public static Jwt createTestJwt(String login) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("preferred_username", login)
                .claim("scope", "read write")
                .build();
    }

    public static Jwt createTestJwt(String login, String... scopes) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("preferred_username", login)
                .claim("scope", String.join(" ", scopes))
                .build();
    }

    public static Account createAccountWithNullBalance(String login) {
        return Account.builder()
                .id(1L)
                .login(login)
                .firstName("Null")
                .lastName("Balance")
                .birthday(LocalDate.of(2000, 1, 1))
                .balance(null)
                .build();
    }

    public static Account createAccountWithNullBirthday(String login) {
        return Account.builder()
                .id(1L)
                .login(login)
                .firstName("Null")
                .lastName("Birthday")
                .birthday(null)
                .balance(BigDecimal.valueOf(100.00))
                .build();
    }

    public static AccountUpdateDto createPartialAccountUpdateDto(String name) {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setName(name);
        return dto;
    }

    public static AccountUpdateDto createPartialAccountUpdateDto(LocalDate birthday) {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setBirthday(birthday);
        return dto;
    }
}
