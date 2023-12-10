package com.peoplein.moiming.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthExceptionValue {

    // AUTH
    AUTH_BAD_LOGIN_INPUT(HttpStatus.BAD_REQUEST, "A100","EMAIL 혹은 PW 값을 전달받지 못했습니다"),
    AUTH_EMAIL_NOT_FOUND(HttpStatus.OK, "A101", "EMAIL 을 찾을 수 없습니다"),
    AUTH_PW_INVALID(HttpStatus.OK, "A102", "PW 가 잘못되었습니다"), // 통신 성공, 정상 응답 송신
    AUTH_EXTRA(HttpStatus.INTERNAL_SERVER_ERROR, "A999", "알 수 없는 오류가 발생하였습니다. 재시도 해주세요");

    private HttpStatus status;
    private String errCode;
    private String errMsg;

    AuthExceptionValue(HttpStatus status, String errCode, String errMsg){
        this.status = status;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
}
