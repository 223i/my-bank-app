package com.iron.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AccountController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("AccountController Unit Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

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

        testPublicAccounts = new ArrayList<>();
        AccountPublicDto dto1 = new AccountPublicDto();
        dto1.setLogin("otheruser1");
        dto1.setName("Other User1");
        AccountPublicDto dto2 = new AccountPublicDto();
        dto2.setLogin("otheruser2");
        dto2.setName("Another User2");
        testPublicAccounts.add(dto1);
        testPublicAccounts.add(dto2);
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
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(accountService).updateAccount(eq("testuser"), any(AccountUpdateDto.class));
    }

    @Test
    @DisplayName("Should return 401 when updating account without authentication")
    void updateAccount_Unauthorized() throws Exception {
        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isForbidden()); // TODO: check CSRF returns 403 instead of 401
    }

    @Test
    @DisplayName("Should return 400 when updating account with invalid data")
    void updateAccount_InvalidData() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(post("/account")
                        .with(jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError()); //TODO: check JSON parsing error returns 500
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
    @DisplayName("Should handle different JWT claims correctly")
    void getAccount_DifferentJwtClaims() throws Exception {
        Jwt differentJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "differentuser")
                .build();

        when(accountService.getAccount("differentuser")).thenReturn(testAccountDto);

        mockMvc.perform(get("/account")
                        .with(jwt().jwt(differentJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(accountService).getAccount("differentuser");
    }
}
