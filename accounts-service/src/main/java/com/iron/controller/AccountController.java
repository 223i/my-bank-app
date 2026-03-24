package com.iron.controller;

import com.iron.dto.AccountDto;
import com.iron.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/account")
    @ResponseStatus(HttpStatus.OK)
    public AccountDto getAccount() {
        return accountService.getAccount();
    }
}
