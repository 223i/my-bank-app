package com.iron.controller;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    // Только для межсервисных вызовов (cash-service, transfer-service) — требует ROLE_ACCOUNTS_INTERNAL
    @PatchMapping("/{login}/decrease-balance")
    @ResponseStatus(HttpStatus.OK)
    public void decrease(@PathVariable String login, @RequestParam BigDecimal amount) {
        log.debug("Decrease balance for {} by {}", login, amount);
        accountService.decreaseBalance(login, amount);
    }

    @PatchMapping("/{login}/increase-balance")
    @ResponseStatus(HttpStatus.OK)
    public void increase(@PathVariable String login, @RequestParam BigDecimal amount) {
        log.debug("Increase balance for {} by {}", login, amount);
        accountService.increaseBalance(login, amount);
    }
}