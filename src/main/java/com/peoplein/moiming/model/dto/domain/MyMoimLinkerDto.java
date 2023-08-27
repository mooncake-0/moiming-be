package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyMoimLinkerDto {

    private MoimRoleType moimRoleType;
    private MoimMemberState memberState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MyMoimLinkerDto(MoimRoleType moimRoleType, MoimMemberState moimMemberState, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.moimRoleType = moimRoleType;
        this.memberState = moimMemberState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public MyMoimLinkerDto(MemberMoimLinker memberMoimLinker) {
        this.moimRoleType = memberMoimLinker.getMoimRoleType();
        this.memberState = memberMoimLinker.getMemberState();
        this.createdAt = memberMoimLinker.getCreatedAt();
        this.updatedAt = memberMoimLinker.getUpdatedAt();
    }
}
