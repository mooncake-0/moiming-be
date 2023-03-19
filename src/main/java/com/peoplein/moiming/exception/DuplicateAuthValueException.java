package com.peoplein.moiming.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DuplicateAuthValueException extends RuntimeException {

    private final String errorCode;

    public DuplicateAuthValueException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
