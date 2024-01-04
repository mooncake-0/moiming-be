package com.peoplein.moiming.exception;

import lombok.Getter;

@Getter
public class MoimingApiException extends RuntimeException{

    private final ExceptionValue ev;

    public MoimingApiException(ExceptionValue ev) {
        super(ev.getErrMsg());
        this.ev = ev;
    }
}
