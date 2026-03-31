package com.iron.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iron.configuration.TestSecurityConfig;
import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AccountController.class, excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class
})
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AccountController Integration Tests")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private com.iron.service.AccountService accountService;

    private Jwt testJwt;
    private AccountDto testAccountDto;
    private AccountUpdateDto testUpdateDto;
    private List<AccountPublicDto> testPublicAccounts;

    @BeforeEach
    void setUp() {
        testJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "testuser")
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

        AccountPublicDto dto1 = new AccountPublicDto();
        dto1.setLogin("otheruser1");
        dto1.setName("Other User1");
        AccountPublicDto dto2 = new AccountPublicDto();
        dto2.setLogin("otheruser2");
        dto2.setName("Another User2");
        testPublicAccounts = List.of(dto1, dto2);
    }

    @Test
    @DisplayName("Should get account successfully")
    void getAccount_Success() throws Exception {
        when(accountService.getAccount("testuser")).thenReturn(testAccountDto);

        mockMvc.perform(get("/account")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.sum").value(100050));

        verify(accountService).getAccount("testuser");
    }

    @Test
    @DisplayName("Should return 401 when getting account without authentication")
    void getAccount_Unauthorized() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should update account successfully")
    void updateAccount_Success() throws Exception {
        when(accountService.updateAccount(eq("testuser"), any(AccountUpdateDto.class)))
                .thenReturn(testAccountDto);

        mockMvc.perform(post("/account")
                        .with(jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(accountService).updateAccount(eq("testuser"), any(AccountUpdateDto.class));
    }

    @Test
    @DisplayName("Should search other accounts successfully")
    void search_Success() throws Exception {
        when(accountService.searchOthersAccounts("testuser")).thenReturn(testPublicAccounts);

        mockMvc.perform(get("/search")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].login").value("otheruser1"))
                .andExpect(jsonPath("$[0].name").value("Other User1"))
                .andExpect(jsonPath("$[1].login").value("otheruser2"))
                .andExpect(jsonPath("$[1].name").value("Another User2"));

        verify(accountService).searchOthersAccounts("testuser");
    }

    @Test
    @DisplayName("Should return empty list when searching with no other accounts")
    void search_EmptyList() throws Exception {
        when(accountService.searchOthersAccounts("testuser")).thenReturn(List.of());

        mockMvc.perform(get("/search")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(accountService).searchOthersAccounts("testuser");
    }

    @Test
    @DisplayName("Should return 401 when searching without authentication")
    void search_Unauthorized() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle account not found")
    void getAccount_NotFound() throws Exception {
        when(accountService.getAccount("testuser"))
                .thenThrow(new RuntimeException("Account not found"));

        mockMvc.perform(get("/account")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isInternalServerError()); // RuntimeException returns 500

        verify(accountService).getAccount("testuser");
    }

    @Test
    @DisplayName("Should handle update failure")
    void updateAccount_Failure() throws Exception {
        when(accountService.updateAccount(eq("testuser"), any(AccountUpdateDto.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(post("/account")
                        .with(jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isInternalServerError());

        verify(accountService).updateAccount(eq("testuser"), any(AccountUpdateDto.class));
    }

    @Test
    @DisplayName("Should handle search failure")
    void search_Failure() throws Exception {
        when(accountService.searchOthersAccounts("testuser"))
                .thenThrow(new RuntimeException("Search failed"));

        mockMvc.perform(get("/search")
                        .with(jwt().jwt(testJwt)))
                .andExpect(status().isInternalServerError());

        verify(accountService).searchOthersAccounts("testuser");
    }
}
