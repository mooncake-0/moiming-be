package com.peoplein.moiming.security.exception;


import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class LoginAttemptException extends AuthenticationException {

    private final AuthExceptionValue exceptionValue;

    public LoginAttemptException(AuthExceptionValue exceptionValue) {
        super(exceptionValue.getErrMsg());
        this.exceptionValue = exceptionValue;
    }

    public LoginAttemptException(AuthExceptionValue exceptionValue, Throwable cause) {
        super(exceptionValue.getErrMsg(), cause);
        this.exceptionValue = exceptionValue;
    }

}
