package com.iron.controller;

import com.iron.exception.TransferException;
import com.iron.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("value") BigDecimal value,
            @RequestParam("login") String loginOfReceiver) {
        String fromLogin = jwt.getClaimAsString("preferred_username");

        try {
            log.info("Executing transfer from {} to {} for amount {}", fromLogin, loginOfReceiver, value);

            transferService.makeTransfer(fromLogin, loginOfReceiver, value);

            model.addAttribute("status", "success");
            model.addAttribute("message", "Перевод успешно выполнен!");
            model.addAttribute("transferAmount", value);
            model.addAttribute("receiver", loginOfReceiver);

        } catch (TransferException e) {
            log.error("Transfer error: {}", e.getMessage());
            model.addAttribute("status", "error");
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "main";
    }
}
