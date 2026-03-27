package com.iron.controller;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.service.AccountService;
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
        return accountService.getAccount(login);
    }

    @PostMapping("/account")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountDto updateAccount(@AuthenticationPrincipal Jwt jwt,
                                      @RequestBody AccountUpdateDto updateDto) {
        String login = jwt.getClaimAsString("preferred_username");
        return accountService.updateAccount(login, updateDto);
    }

    @GetMapping("/search")
    public List<AccountPublicDto> search(@AuthenticationPrincipal Jwt jwt) {
        String myLogin = jwt.getClaimAsString("preferred_username");
        return accountService.searchOthersAccounts(myLogin);
    }

    @PatchMapping("/{login}/decrease-balance")
    public void decrease(@PathVariable String login, @RequestParam BigDecimal amount) {
        accountService.decreaseBalance(login, amount);
    }

    @PatchMapping("/{login}/increase-balance")
    public void increase(@PathVariable String login, @RequestParam BigDecimal amount) {
        accountService.increaseBalance(login, amount);
    }
}
