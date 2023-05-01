package com.peoplein.moiming.domain.enums;

// 각 Type 별 알림의 종류
public enum NotificationDomainCategory {

    // 모임 가입 완료
    MOIM_NEW_MEMBER,
    MOIM_DECLINE_MEMBER,

    // 모임 강퇴
    MOIM_BAN_MEMBER,

    // 일정 생성
    SCHEDULE_NEW,

    // 게시물 작성됨
    POST_NEW,

    // 댓글 달림
    POST_COMMENT,


    // 기타
    DEFAULT
}
