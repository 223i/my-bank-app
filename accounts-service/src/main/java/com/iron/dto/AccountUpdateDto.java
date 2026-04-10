package com.iron.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Birthday is required")
    @PastOrPresent(message = "Birthday must be in the past or present")
    private LocalDate birthday;
}