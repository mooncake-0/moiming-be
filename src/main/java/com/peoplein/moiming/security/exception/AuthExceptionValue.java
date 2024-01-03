package com.peoplein.moiming.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthExceptionValue {

    AUTH_COMMON_INVALID_PARAM_NULL(HttpStatus.BAD_REQUEST.value(), "AC000", "잘못된 Parameter 전달: NULL 발생"),

    AUTH_LOGIN_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED.value(), "AL000", "비밀번호가 일치하지 않습니다"),
    AUTH_LOGIN_REQUEST_INVALID(HttpStatus.BAD_REQUEST.value(), "AL001", "요청을 읽을 수 없습니다"),
    AUTH_LOGIN_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "AL002", "로그인 시도한 Email 을 찾을 수 없습니다"),
    AUTH_LOGIN_EXTRA(HttpStatus.UNAUTHORIZED.value(), "AL999", "알 수 없는 이유로 로그인에 실패하였습니다"),


    AUTH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED.value(), "AT000", "인증이 필요한 요청에 Access Token 이 존재하지 않습니다"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "AT001", "Access Token 이 만료되었습니다"),
    AUTH_TOKEN_VERIFICATION_FAIL(HttpStatus.UNAUTHORIZED.value(), "AT002", "유효하지 않은 Access Token 입니다"),
    AUTH_LOGOUT_TOKEN_TRIAL(HttpStatus.UNAUTHORIZED.value(), "AT003", "로그아웃 관리 대상인 이유로 Access Token 을 사용할 수 없습니다"),
    AUTH_TOKEN_EXTRA(HttpStatus.UNAUTHORIZED.value(), "AT999", "알 수 없는 이유로 Access Token 인증에 실패하였습니다"),


    AUTH_REFRESH_TOKEN_NOT_MATCH(HttpStatus.UNAUTHORIZED.value(), "ART000", "Member 에 저장된 Refresh Token 이 일치하지 않아 삭제됩니다. 다시 로그인 해주세요"), // 재발급 요청에 대한 클라이언트 미인증, 허용할 수 없음
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "ART001", "Refresh Token 이 만료되었습니다"),
    AUTH_REFRESH_TOKEN_VERIFICATION_FAIL(HttpStatus.BAD_REQUEST.value(), "ART002", "유효하지 않은 Refresh Token 입니다"),
    AUTH_REFRESH_TOKEN_EXTRA(HttpStatus.UNAUTHORIZED.value(), "ART999", "알 수 없는 이유로 Refresh Token 인증에 실패했습니다"),


    AUTH_SIGN_IN_NICKNAME_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AS000", "자동 닉네임 생성에 실패하였습니다. 요청을 다시 시도해주세요"),
    AUTH_SIGN_IN_DUPLICATE_COLUMN(HttpStatus.CONFLICT.value(), "AS001", "중복되는 회원입니다 (이메일, 전화번호, CI 중 중복 발생)");


    private int status;
    private String errCode;
    private String errMsg;

    AuthExceptionValue(int status, String errCode, String errMsg){
        this.status = status;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
}
