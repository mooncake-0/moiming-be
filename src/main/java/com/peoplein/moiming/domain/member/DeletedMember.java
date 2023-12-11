package com.peoplein.moiming.domain.member;

import lombok.Getter;

@Getter
public class DeletedMember extends Member{

    private Long memberId;
    private String nickname;

    public DeletedMember(Long memberId) {

        this.memberId = memberId;
        this.nickname = "탈퇴한 사용자";
    }
}
