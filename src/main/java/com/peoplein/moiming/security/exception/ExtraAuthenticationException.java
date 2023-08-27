package com.peoplein.moiming.security.exception;

import org.springframework.security.core.AuthenticationException;

public class ExtraAuthenticationException extends AuthenticationException {

    public ExtraAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ExtraAuthenticationException(String msg) {
        super(msg);
    }
}
