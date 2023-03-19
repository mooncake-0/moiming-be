package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleMemberDto {

    private ScheduleMemberState memberState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MoimMemberInfoDto moimMemberInfoDto;

    public ScheduleMemberDto(ScheduleMemberState memberState, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.memberState = memberState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void setMoimMemberInfoDto(MoimMemberInfoDto moimMemberInfoDto) {
        this.moimMemberInfoDto = moimMemberInfoDto;
    }
}
