package com.iron.mapper;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.model.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountDto toDto(Account entity) {
        if (entity == null) {
            return null;
        }
        
        return AccountDto.builder()
                .name(entity.getFirstName() + " " + entity.getLastName())
                .birthday(entity.getBirthday() != null ? entity.getBirthday().atStartOfDay().atOffset(java.time.ZoneOffset.UTC) : null)
                .sum(entity.getBalance() != null ? entity.getBalance().longValue() : null)
                .build();
    }

    @Override
    public void updateEntity(AccountUpdateDto dto, Account entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getName() != null) {
            String[] names = dto.getName().split(" ", 2);
            entity.setFirstName(names.length > 0 ? names[0] : entity.getFirstName());
            entity.setLastName(names.length > 1 ? names[1] : entity.getLastName());
        }
        if (dto.getBirthday() != null) {
            entity.setBirthday(dto.getBirthday());
        }
    }

    @Override
    public AccountPublicDto toPublicDto(Account account) {
        if (account == null) {
            return null;
        }
        
        AccountPublicDto dto = new AccountPublicDto();
        dto.setLogin(account.getLogin());
        dto.setName(account.getFirstName() + " " + account.getLastName());
        return dto;
    }

    @Override
    public List<AccountPublicDto> toPublicDtoList(List<Account> accounts) {
        if (accounts == null) {
            return null;
        }
        return accounts.stream()
                .map(this::toPublicDto)
                .collect(Collectors.toList());
    }
}
