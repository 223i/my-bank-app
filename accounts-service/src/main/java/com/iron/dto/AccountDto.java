package com.iron.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class AccountDto {

    private String login;
    private String name;
    private OffsetDateTime birthday;
    private Long sum;
    private List<AccountDto> accounts;
}
