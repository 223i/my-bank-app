package com.iron.controller;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/account")
    @ResponseStatus(HttpStatus.OK)
    public AccountDto getAccount(@AuthenticationPrincipal Jwt jwt) {
        String login = jwt.getClaimAsString("preferred_username");
        log.debug("Get account for login {}", login);
        return accountService.getAccount(login);
    }

    @PostMapping("/account")
    @ResponseStatus(HttpStatus.OK)
    public AccountDto updateAccount(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody AccountUpdateDto updateDto) {
        String login = jwt.getClaimAsString("preferred_username");
        log.debug("Update account for login {}", login);
        return accountService.updateAccount(login, updateDto);
    }

    @GetMapping("/search")
    public List<AccountPublicDto> search(@AuthenticationPrincipal Jwt jwt) {
        String myLogin = jwt.getClaimAsString("preferred_username");
        return accountService.searchOthersAccounts(myLogin);
    }

    @PreAuthorize("#login == authentication.name")
    @PatchMapping("/{login}/decrease-balance")
    @ResponseStatus(HttpStatus.OK)
    public void decrease(@PathVariable String login,
                         @RequestParam BigDecimal amount,
                         @RequestParam String transactionId) {
        log.debug("Decrease balance for {} by {}, transactionId={}", login, amount, transactionId);
        accountService.decreaseBalance(login, amount, transactionId);
    }

    @PreAuthorize("#login == authentication.name")
    @PatchMapping("/{login}/increase-balance")
    @ResponseStatus(HttpStatus.OK)
    public void increase(@PathVariable String login,
                         @RequestParam BigDecimal amount,
                         @RequestParam String transactionId) {
        log.debug("Increase balance for {} by {}, transactionId={}", login, amount, transactionId);
        accountService.increaseBalance(login, amount, transactionId);
    }
}