package com.peoplein.moiming.domain.enums;

public enum MoimMemberState {

    ACTIVE,
    IBW, // INACTIVE_BY_WILL (자의로 나간 유저)
    IBF, // INACTIVE_BY_FORCE (강퇴 유저)
    DORMANT, // 휴면 유저 (휴면은 전상태를 기억해야한다)
    NOTFOUND, // 탈퇴 유저
}

// IBW --> ACTIVE (가능) 모임 재가입
// IBF --> ACTIVE (불가능)
// DORMANT --> ACTIVE (가능) 계정 활성화
// NOTFOUND --> ACTIVE (불가능)


// ACTIVE --> IBW (가능) 모임 나가기
// IBF --> IBW (불가능)
// DORMANT --> IBW (불가능 : ACTIVE 로 변경해야함)
// NOTFOUND --> IBW (불가능)


// ACTIVE --> IBF (가능)  강퇴
// IBW --> IBF  (불가능)
// DORMANT --> IBF (가능) 휴면계정 강퇴 가능
// NOTFOUND --> IBF (불가능)

// ACTIVE --> DORMANT (가능) 휴면계정전환
// IBW --> DORMANT (가능) 휴면계정전환
// IBF --> DORMANT (불가능) - 논의 필요
// NOTFOUND --> DORMANT (불가능)

// ACTIVE --> NOTFOUND (가능) 계정 삭제
// IBW --> NOTFOUND (가능) 계정 삭제
// IBF --> NOTFOUND (가능) 계정 삭제
// DORMANT --> NOTFOUND (가능) 계정 삭제