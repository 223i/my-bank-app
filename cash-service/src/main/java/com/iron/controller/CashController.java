package com.iron.controller;

import com.iron.service.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/operation")
    @ResponseStatus(HttpStatus.OK)
    public void handleOperation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("value") BigDecimal value,
            @RequestParam("type") String type) {
        String login = jwt.getClaimAsString("preferred_username");
        log.info("Cash operation for login={}, type={}, amount={}", login, type, value);
        cashService.processOperation(login, value, type);
    }
}