package com.peoplein.moiming.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class MoimingValidationException extends RuntimeException {
    private String errCode;
    private final Map<String, String> errMap;

    public MoimingValidationException(String errCode, String errMsg, Map<String, String> errMap) {
        super(errMsg);
        this.errCode = errCode;
        this.errMap = errMap;
    }

    public MoimingValidationException(ExceptionValue exceptionValue, Map<String, String> errMap) {
        super(exceptionValue.getErrMsg());
        this.errCode = exceptionValue.getErrCode();
        this.errMap = errMap;
    }
}