package com.iron.service;

import com.iron.dto.AccountDto;
import com.iron.mapper.AccountMapper;
import com.iron.model.Account;
import com.iron.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper mapper;
    private final AccountRepository accountRepository;

    //TODO: implement
    public AccountDto getAccount() {
        Account account = accountRepository.findAll().stream()
                .findFirst().orElseThrow(() -> new RuntimeException("No account found"));
        return mapper.toDto(account);
    }
}
