package com.peoplein.moiming.exception;

import lombok.Getter;

@Getter
public enum ExceptionValue {

    // COMMON
    COMMON_INVALID_PARAM("C100", "잘못된 Paremeter 전달: 잘못된 상황 발생"),
    COMMON_INVALID_PARAM_NULL("C101", "잘못된 Parameter 전달: NULL 발생"),
    COMMON_REQUEST_VALIDATION("C102", "Request DTO Validation 실패"),
    COMMON_INVALID_SITUATION("C999", "발생할 가능성이 없는 상황 발생, log 확인 필요"),


    // MOIM DOMAIN
    MOIM_NOT_FOUND("M100", "해당 모임을 찾을 수 없습니다"),
    MOIM_POST_NOT_FOUND("MP100", "해당 게시물을 찾을 수 없습니다"),
    MOIM_MEMBER_NOT_FOUND("MM100", "모임의 모임원이 아닙니다"),
    MOIM_MEMBER_NOT_ACTIVE("MM101", "활동중인 모임원이 아닙니다"),
    MOIM_MEMBER_NOT_AUTHORIZED("MM102", "해당 요청의 권한이 없는 모임원입니다"),
    MOIM_POST_COMMENT_NOT_FOUND("MC100", "관련 댓글을 찾을 수 없습니다"),


    // Member Domain
    MEMBER_NOT_FOUND("MEM100", "해당 유저를 찾을 수 없습니다"),

    MEMBER_LOGOUT_AT_NOT_FOUND("MA100", "(Security 에서 짤리는 요청, 발생불가) 액세스 토큰 없이 도달"),
    MEMBER_LOGOUT_AT_DUPLICATE("MA101", "이미 로그아웃 처리된 인증 토큰입니다");


    private String errCode;
    private String errMsg;

    ExceptionValue(String errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
}
