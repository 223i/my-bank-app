package com.iron.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TransferException extends RuntimeException {

    private final String errorCode;

    public TransferException(String message) {
        super(message);
        this.errorCode = "TRANSFER_ERROR";
    }

    public TransferException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
