package com.iron.mapper;

import com.iron.dto.AccountDto;
import com.iron.dto.AccountPublicDto;
import com.iron.dto.AccountUpdateDto;
import com.iron.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDto toDto(Account entity);

    void updateEntity(AccountUpdateDto dto, @MappingTarget Account entity);

    AccountPublicDto toPublicDto(Account account);

    List<AccountPublicDto> toPublicDtoList(List<Account> accounts);
}
