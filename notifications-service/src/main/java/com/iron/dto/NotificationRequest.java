package com.iron.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationRequest {
    private String recipientLogin;
    private String message;
    private String type;
}
