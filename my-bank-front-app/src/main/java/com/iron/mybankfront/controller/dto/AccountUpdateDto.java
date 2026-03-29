package com.iron.mybankfront.controller.dto;

import java.time.LocalDate;

public record AccountUpdateDto (String name, LocalDate birthday) {
}