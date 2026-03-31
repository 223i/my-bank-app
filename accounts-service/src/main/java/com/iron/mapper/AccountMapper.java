package com.iron.mapper;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AccountMapper {

    @Mapping(target = "name", expression = "java(combineName(entity))")
    @Mapping(target = "birthday", expression = "java(toOffsetDateTime(entity.getBirthday()))")
    @Mapping(target = "sum", expression = "java(entity.getBalance() != null ? entity.getBalance().longValue() : 0L)")
    @Mapping(target = "accounts", ignore = true)
    public abstract AccountDto toDto(Account entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "firstName", expression = "java(extractFirstName(dto.getName()))")
    @Mapping(target = "lastName", expression = "java(extractLastName(dto.getName()))")
    @Mapping(target = "birthday", source = "birthday")
    public abstract void updateEntity(AccountUpdateDto dto, @MappingTarget Account entity);

    @Mapping(target = "name", expression = "java(combineName(account))")
    public abstract AccountPublicDto toPublicDto(Account account);

    public abstract List<AccountPublicDto> toPublicDtoList(List<Account> accounts);

    protected String combineName(Account account) {
        String first = account.getFirstName() != null ? account.getFirstName() : "";
        String last = account.getLastName() != null ? account.getLastName() : "";
        return (first + " " + last).trim();
    }

    protected OffsetDateTime toOffsetDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
    }

    protected String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return null;
        int spaceIdx = fullName.indexOf(' ');
        return spaceIdx > 0 ? fullName.substring(0, spaceIdx) : fullName;
    }

    protected String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) return null;
        int spaceIdx = fullName.indexOf(' ');
        return spaceIdx > 0 ? fullName.substring(spaceIdx + 1) : "";
    }
}