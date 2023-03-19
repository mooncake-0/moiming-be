package com.peoplein.moiming.security.exception;

import org.springframework.http.HttpStatus;

public enum AuthErrorEnum {

    AUTH_SIGNIN_INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "AS001", "UID_OR_PW_OR_EMAIL"),
    AUTH_SIGNIN_DUPLICATE_UID(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AS002", "DUP_UID"),
    AUTH_SIGNIN_DUPLICATE_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AS003", "DUP_EMAIL"),
    AUTH_SIGNIN_NICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AS004", "NICE_FAILED"),
    AUTH_SIGNIN_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AS000", "AUTH_SIGNIN_UNKNOWN"),

    AUTH_LOGIN_INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "AL001", "UID_OR_PW"),
    AUTH_LOGIN_PW_ERROR(HttpStatus.UNAUTHORIZED.value(), "AL002", "PW_ERR"),
    AUTH_LOGIN_DISABLED_ACCOUNT(HttpStatus.UNAUTHORIZED.value(), "AL003", "DORMANT"),
    AUTH_LOGIN_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AL000", "AUTH_LOGIN_UNKNOWN"),

    AUTH_JWT_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "AJ001", "TOKEN_EXP"),
    AUTH_JWT_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED.value(), "AJ002", "TOKEN_VERIFY_FAIL"),

    // 최종 처리
    AUTH_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR.value(), "A000", "AUTH_UNKNOWN");

    private final int statusCode;
    private final String errorCode;
    private final String errorType;

    AuthErrorEnum(int statusCode, String errorCode, String errorType) {
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

