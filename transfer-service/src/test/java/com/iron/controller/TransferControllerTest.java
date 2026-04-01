package com.iron.controller;

import com.iron.exception.TransferException;
import com.iron.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TransferController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("TransferController Tests")
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Test
    @DisplayName("Should return 200 and call service on valid transfer request")
    void transfer_success() throws Exception {
        mockMvc.perform(post("/transfer")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "jdoe")))
                        .param("value", "100")
                        .param("login", "alice_99"))
                .andExpect(status().isOk());

        verify(transferService).makeTransfer("jdoe", "alice_99", new BigDecimal("100"));
    }

    @Test
    @DisplayName("Should return 400 when TransferException is thrown")
    void transfer_badRequest_onTransferException() throws Exception {
        doThrow(new TransferException("Insufficient funds"))
                .when(transferService).makeTransfer(any(), any(), any());

        mockMvc.perform(post("/transfer")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "jdoe")))
                        .param("value", "100")
                        .param("login", "alice_99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    @DisplayName("Should return 401 when no JWT token provided")
    void transfer_unauthorized_whenNoAuth() throws Exception {
        mockMvc.perform(post("/transfer")
                        .param("value", "100")
                        .param("login", "alice_99"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(transferService);
    }

    @Test
    @DisplayName("Should extract login from JWT preferred_username claim")
    void transfer_extractsLoginFromJwt() throws Exception {
        mockMvc.perform(post("/transfer")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "bob_wilson")))
                        .param("value", "50")
                        .param("login", "alice_99"))
                .andExpect(status().isOk());

        verify(transferService).makeTransfer(eq("bob_wilson"), eq("alice_99"), eq(new BigDecimal("50")));
    }
}