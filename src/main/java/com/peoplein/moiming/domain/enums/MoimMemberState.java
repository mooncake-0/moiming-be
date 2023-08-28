package com.peoplein.moiming.domain.enums;

public enum MoimMemberState {

    ACTIVE,
    IBW, // INACTIVE_BY_WILL (자의로 나간 유저)
    IBF, // INACTIVE_BY_FORCE (강퇴 유저)
    DORMANT, // 휴면 유저
    NOTFOUND, // 탈퇴 유저
}
