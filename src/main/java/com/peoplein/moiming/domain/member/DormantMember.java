package com.peoplein.moiming.domain.member;

import lombok.Getter;

@Getter
public class DormantMember extends Member{

    private Long memberId;
    private String nickname;

    public DormantMember(Member member) {

        this.memberId = member.getId();
        this.nickname = "휴면 전환 사용자";
    }

}
