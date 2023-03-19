package com.peoplein.moiming.exception.enums;

import org.springframework.http.HttpStatus;

/*
 Spring 에서 지정하는 Response Exception 및 추가 Custom Exception 관리
 */
public enum ResponseErrorEnum {

    RESPONSE_HTTP_NOT_READABLE(HttpStatus.BAD_REQUEST.value(), "R001", "HTTP_NOT_READABLE"),
    RESPONSE_HTTP_NOT_WRITABLE(HttpStatus.INTERNAL_SERVER_ERROR.value(), "R002", "HTTP_NOT_WRITABLE");

    private final int statusCode;
    private final String errorCode;
    private final String errorType;

    ResponseErrorEnum(int statusCode, String errorCode, String errorType) {
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
