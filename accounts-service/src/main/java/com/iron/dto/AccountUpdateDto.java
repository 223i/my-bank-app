package com.iron.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountUpdateDto {
    private String name;
    private LocalDate birthday;
}