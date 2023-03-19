package com.peoplein.moiming.exception.enums;

import org.springframework.http.HttpStatus;

/*
 가장 일반적 RunTimeException 오류 관리
 */
public enum CommonErrorEnum {

    COMMON_NULL_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR.value(), "C001", "NULL_POINTER"),
    COMMON_ILLEGAL_PARAMS(HttpStatus.BAD_REQUEST.value(), "C002", "BAD_PARAMS");

    private final int statusCode;
    private final String errorCode;
    private final String errorType;

    CommonErrorEnum(int statusCode, String errorCode, String errorType) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

}

