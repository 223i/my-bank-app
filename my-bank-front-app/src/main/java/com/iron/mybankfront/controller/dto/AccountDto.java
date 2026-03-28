package com.iron.mybankfront.controller.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AccountDto(String login, String name, OffsetDateTime birthday,
                         Long sum, List<AccountDto> accounts) {
}
