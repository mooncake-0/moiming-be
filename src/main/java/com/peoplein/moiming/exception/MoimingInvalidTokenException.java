package com.peoplein.moiming.exception;

public class MoimingInvalidTokenException extends RuntimeException{

    public MoimingInvalidTokenException(String message) {
        super(message);
    }

    public MoimingInvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
