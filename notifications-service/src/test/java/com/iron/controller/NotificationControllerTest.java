package com.iron.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iron.configuration.TestSecurityConfig;
import com.iron.dto.NotificationRequest;
import com.iron.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void shouldReceiveNotificationWhenRoleIsPresent() throws Exception {
        NotificationRequest request = new NotificationRequest(
                "user_login",
                "Test Message",
                "TRANSFER"
        );

        doNothing().when(notificationService).save(request);

        mockMvc.perform(post("/api/notifications/send")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NOTIFICATIONS_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).save(request);
    }

    @Test
    void shouldReturnForbiddenWhenRoleIsMissing() throws Exception {
        NotificationRequest request = new NotificationRequest("user", "msg", "type");

        mockMvc.perform(post("/api/notifications/send")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_WRONG_ROLE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldReturnUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(notificationService);
    }
}
