package com.peoplein.moiming.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class MoimingValidationException extends RuntimeException {
    private final Map<String, String> errMap;

    public MoimingValidationException(String errMsg, Map<String, String> errMap) {
        super(errMsg);
        this.errMap = errMap;
    }

}