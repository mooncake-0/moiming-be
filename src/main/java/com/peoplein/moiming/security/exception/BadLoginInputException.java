package com.peoplein.moiming.security.exception;


import org.springframework.security.core.AuthenticationException;

public class BadLoginInputException extends AuthenticationException {

    public BadLoginInputException(String msg) {
        super(msg);
    }

    public BadLoginInputException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
