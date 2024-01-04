package com.peoplein.moiming.security.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;

public class LogoutTokenTrialException extends JWTVerificationException {

    public LogoutTokenTrialException(String message) {
        super(message);
    }

    public LogoutTokenTrialException(String message, Throwable cause) {
        super(message, cause);
    }
}