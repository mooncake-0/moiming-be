package com.peoplein.moiming.exception.repository;

public class InvalidQueryParameterException extends RuntimeException {

    public InvalidQueryParameterException(String message) {
        super(message);
    }

    public InvalidQueryParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
