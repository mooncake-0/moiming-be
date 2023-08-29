package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyMoimLinkerDto {

    private MoimMemberRoleType moimMemberRoleType;
    private MoimMemberState memberState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MyMoimLinkerDto(MoimMemberRoleType moimMemberRoleType, MoimMemberState moimMemberState, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.moimMemberRoleType = moimMemberRoleType;
        this.memberState = moimMemberState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public MyMoimLinkerDto(MoimMember moimMember) {
        this.moimMemberRoleType = moimMember.getMoimMemberRoleType();
        this.memberState = moimMember.getMemberState();
        this.createdAt = moimMember.getCreatedAt();
        this.updatedAt = moimMember.getUpdatedAt();
    }
}
