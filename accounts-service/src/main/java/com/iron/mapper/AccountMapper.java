package com.iron.mapper;

import com.iron.dto.AccountDto;
import com.iron.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {


//    @Mapping(target = "id", ignore = true)
//    Account toEntity(AccountRequestDto dto);

    AccountDto toDto(Account entity);

}
