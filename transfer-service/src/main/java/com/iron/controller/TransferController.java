package com.iron.controller;

import com.iron.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.OK)
    public void transfer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("value") BigDecimal value,
            @RequestParam("login") String loginOfReceiver) {
        String fromLogin = jwt.getClaimAsString("preferred_username");
        log.info("Transfer from {} to {} for amount {}", fromLogin, loginOfReceiver, value);
        transferService.makeTransfer(fromLogin, loginOfReceiver, value);
    }
}