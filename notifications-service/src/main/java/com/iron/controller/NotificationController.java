package com.iron.controller;

import com.iron.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @PostMapping("/send")
    public ResponseEntity<Void> receiveNotification(@RequestBody NotificationRequest request) {
        log.info("[NOTIFICATION SERVICE] To: {} | Type: {} | Message: {}",
                request.getRecipientLogin(),
                request.getType(),
                request.getMessage());
        return ResponseEntity.ok().build();
    }
}
