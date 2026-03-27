package com.iron.configuration;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public class TestAccountMapper {

    public AccountDto toDto(Account entity) {
        if (entity == null) {
            return null;
        }
        return AccountDto.builder()
                .name(entity.getFirstName() + " " + entity.getLastName())
                .birthday(OffsetDateTime.from(entity.getBirthday().atStartOfDay().atOffset(java.time.ZoneOffset.UTC)))
                .sum(entity.getBalance().longValue())
                .accounts(List.of())
                .build();
    }

    public void updateEntity(AccountUpdateDto dto, @MappingTarget Account entity) {
        // Since Account is immutable with @Builder, we can't update it directly
        // This method is not used in the current test scenarios
    }

    public AccountPublicDto toPublicDto(Account account) {
        if (account == null) {
            return null;
        }
        AccountPublicDto dto = new AccountPublicDto();
        dto.setLogin(account.getLogin());
        dto.setName(account.getFirstName() + " " + account.getLastName());
        return dto;
    }

    public List<AccountPublicDto> toPublicDtoList(List<Account> accounts) {
        if (accounts == null) {
            return List.of();
        }
        return accounts.stream()
                .map(this::toPublicDto)
                .toList();
    }
}
