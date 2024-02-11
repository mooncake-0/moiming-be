package com.peoplein.moiming.exception;

import com.peoplein.moiming.security.exception.AuthExceptionValue;

public class MoimingAuthNoRollbackException extends MoimingAuthApiException {

    public MoimingAuthNoRollbackException(AuthExceptionValue ev) {
        super(ev);
    }

    public MoimingAuthNoRollbackException(AuthExceptionValue ev, Throwable exception) {
        super(ev, exception);
    }
}
