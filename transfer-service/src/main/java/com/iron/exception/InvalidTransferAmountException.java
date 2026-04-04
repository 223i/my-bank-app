package com.iron.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransferAmountException extends TransferException {

    private final String errorCode;

    public InvalidTransferAmountException(String message) {
        super(message);
        this.errorCode = "TRANSFER_ERROR";
    }

    public InvalidTransferAmountException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
