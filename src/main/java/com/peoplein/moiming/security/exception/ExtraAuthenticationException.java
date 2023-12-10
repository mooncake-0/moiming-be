package com.peoplein.moiming.security.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class ExtraAuthenticationException extends AuthenticationException {

    private final AuthExceptionValue exceptionValue;

    public ExtraAuthenticationException(AuthExceptionValue exceptionValue, Throwable cause) {
        super(exceptionValue.getErrMsg(), cause);
        this.exceptionValue = exceptionValue;
    }

    public ExtraAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
        this.exceptionValue = null;
    }
}
