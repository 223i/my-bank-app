package com.iron.controller;

import com.iron.service.CashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/operation")
    public String handleOperation(
            Model model,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("value") BigDecimal value,
            @RequestParam("type") String type
    ) {
        String login = jwt.getClaimAsString("preferred_username");

        try {
            cashService.processOperation(login, value, type);
            model.addAttribute("message", "Операция выполнена успешно");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "main";
    }
}