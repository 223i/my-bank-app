package com.iron.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfTransferException extends TransferException {

    private final String errorCode;

    public SelfTransferException(String message) {
        super(message);
        this.errorCode = "TRANSFER_ERROR";
    }

    public SelfTransferException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
