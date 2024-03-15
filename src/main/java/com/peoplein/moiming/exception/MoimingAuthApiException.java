package com.peoplein.moiming.exception;

import com.peoplein.moiming.security.exception.AuthExceptionValue;
import lombok.Getter;

/*
 Security 단을 벗어나
 Spring Layer 내에서 발생하는 Auth 예외, 인증 없는 요청 인 앱 대응
 */
@Getter
public class MoimingAuthApiException extends RuntimeException {

    private final AuthExceptionValue ev;

    public MoimingAuthApiException(AuthExceptionValue ev) {
        super(ev.getErrMsg());
        this.ev = ev;
    }

    public MoimingAuthApiException(AuthExceptionValue ev, Throwable exception) {
        super(ev.getErrMsg(), exception);
        this.ev = ev;
        exception.printStackTrace();
    }
}