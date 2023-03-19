package com.peoplein.moiming.exception;

public class BadAuthParameterInputException extends RuntimeException {

    private final String errorCode;

    public BadAuthParameterInputException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }
}
