package com.peoplein.moiming.domain.enums;

public enum MoimMemberState {

    ACTIVE,
    IBW, // INACTIVE_BY_WILL (자의로 나간 유저)
    IBF, // INACTIVE_BY_FORCE (타의로 나간 유저)
    IBF_BY_VIOLATION,
    IBF_BY_NO_GREETING,


    WAIT, // 그냥 대기 (없을 듯)
    WAIT_BY_AGE, // 나이 조건 미충족
    WAIT_BY_GENDER, // 성별 조건 미충족
    WAIT_BY_MOIM_CNT, // 모임 갯수 제한 미충족
    WAIT_BY_DUP, // 겸직 미충족

    DORMANT,
    NOTFOUND // 탈퇴
}
