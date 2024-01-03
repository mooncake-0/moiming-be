package com.peoplein.moiming.exception;

import com.google.api.Http;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionValue {

    COMMON_INVALID_PARAM(HttpStatus.BAD_REQUEST.value(), "C000", "잘못된 Parameter 전달 (NULL or Invalid)"),
    COMMON_REQUEST_VALIDATION(HttpStatus.BAD_REQUEST.value(), "C001", "잘못된 Dto Value 확인, Response Body 를 확인하세요"),
    COMMON_INVALID_SITUATION(HttpStatus.UNPROCESSABLE_ENTITY.value(), "C999", "잘못된 상황이 발생했습니다. 로그를 확인하세요"),

    // MOIM DOMAIN
    MOIM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "M000", "해당 모임을 찾을 수 없습니다"),

    MOIM_JOIN_FAIL_BY_AGE_RULE(HttpStatus.FORBIDDEN.value(), "M001", "가입 조건 중 나이가 부합하지 않아 가입할 수 없습니다"),
    MOIM_JOIN_FAIL_BY_GENDER_RULE(HttpStatus.FORBIDDEN.value(), "M002", "가입 조건 중 성별이 부합하지 않아 가입할 수 없습니다"),
    MOIM_JOIN_FAIL_BY_MEMBER_FULL(HttpStatus.FORBIDDEN.value(), "M003", "모임 정원이 가득차서 가입할 수 없습니다"),

    MOIM_LEAVE_FAIL_BY_LAST_MEMBER(HttpStatus.FORBIDDEN.value(), "M004", "마지막 회원으로, 모임을 나갈 수 없습니다 (MVP 에선 발생할 일 없음)"),
    MOIM_LEAVE_FAIL_BY_MANAGER(HttpStatus.FORBIDDEN.value(), "M005", "운영자(개설자) 는 모임을 나갈 수 없습니다"),

    MOIM_POST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "MP000", "해당 게시물을 찾을 수 없습니다"),

    MOIM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "MM000", "모임의 모임원이 아닙니다"),
    MOIM_MEMBER_NOT_ACTIVE(HttpStatus.FORBIDDEN.value(), "MM001", "활동중인 모임원이 아닙니다"),
    MOIM_MEMBER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN.value(), "MM002", "해당 요청의 권한이 없는 모임원입니다"),
    MOIM_MEMBER_JOIN_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "MM003", "재가입이 불가능한 유저입니다"),
    MOIM_MEMBER_STATE_CHANGE_FAIL(HttpStatus.UNPROCESSABLE_ENTITY.value(), "MM004", "해당 상태로 변경이 불가능합니다"),
    MOIM_MEMBER_ROLE_GRANT_FAIL(HttpStatus.FORBIDDEN.value(), "MM005", "해당 모임원에게 권한 부여가 불가능합니다 (MVP 에선 발생할 일 없음)"),

    MOIM_POST_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "MC000", "관련 댓글을 찾을 수 없습니다"),

    // Member Domain
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "MEM000", "해당 유저를 찾을 수 없습니다"),

    // Member Policy
    MEMBER_POLICY_ESSENTIAL(HttpStatus.BAD_REQUEST.value(), "MEMP000", "동의 필수 약관입니다"),
    MEMBER_POLICY_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "MEMP001", "해당 약관 동의 여부를 수정할 권한이 없습니다");


    private int status;
    private String errCode;
    private String errMsg;

    ExceptionValue(int status, String errCode, String errMsg){
        this.status = status;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
}
