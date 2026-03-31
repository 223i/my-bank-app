package com.iron.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class TransferExceptionHandler {

    public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}

    @ExceptionHandler(TransferException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTransferError(TransferException ex) {
        log.warn("Transfer rejected: {}", ex.getMessage());
        return new ErrorResponse(400, ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unexpected error in transfer-service", ex);
        return new ErrorResponse(500, "Internal server error", LocalDateTime.now());
    }
}