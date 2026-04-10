package com.iron.controller;

import com.iron.dto.NotificationRequest;
import com.iron.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> receiveNotification(@RequestBody NotificationRequest request) {
        notificationService.save(request);
        return ResponseEntity.ok().build();
    }
}