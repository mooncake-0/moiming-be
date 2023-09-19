package com.peoplein.moiming.domain.enums;

public enum MoimMemberState {

    ACTIVE,
    IBW, // INACTIVE_BY_WILL (자의로 나간 유저)
    IBF, // INACTIVE_BY_FORCE (강퇴 유저)
    IBD, // INACTIVE_BY_DORMANT (휴면으로 나가진 유저)
    NOTFOUND, // 탈퇴 유저
}

// ACTIVE   > IBW       (가능 : 모임 나가기)
//          > IBF       (가능 : 강퇴)
//          > IBD       (가능 : 휴면 전환)
//          > NOTFOUND  (가능 : 계정 삭제)

// IBW      > ACTIVE    (가능 : 모임 재가입)
//          > IBF       (불가능)
//          > IBD       (가능 : 휴면 전환) > 개념적으로 더 큰 쪽으로는 전환 가능
//          > NOTFOUND  (가능 : 계정 삭제)

// IBF      > ACTIVE    (불가능 : 강퇴 유저 재가입 불가)
//          > IBW       (불가능)
//          > IBD       (불가능 : 휴면이 되어도 강퇴 낙인은 지워지지 않는다)
//          > NOTFOUND  (가능 : 계정을 삭제한다면 상태가 변경된다)


// IBD      > ACTIVE    (가능 : 휴면게정 복구 후 재가입)
//          > IBW       (불가능 : 휴면으로 나가진 상태에서 바뀔 수 없다, 변경이 되는 경우는 ACTIVE 로 먼저 바뀌어야 함)
//          > IBF       (불가능 : 위와 동일)
//          > NOTFOUND  (가능 : 계정 삭제시 변경된다)

// NOTFOUND (어떠한 상태로도 변경 불가능하다)