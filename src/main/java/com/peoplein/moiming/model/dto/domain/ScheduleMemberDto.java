package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.MemberScheduleLinker;
import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import com.peoplein.moiming.temp.ScheduleService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static ScheduleMemberDto create(MemberScheduleLinker memberScheduleLinker,
                                           MoimMemberInfoDto moimMemberInfoDto) {
        ScheduleMemberDto scheduleMemberDto = new ScheduleMemberDto(
                memberScheduleLinker.getMemberState(),
                memberScheduleLinker.getCreatedAt(),
                memberScheduleLinker.getUpdatedAt());
        scheduleMemberDto.setMoimMemberInfoDto(moimMemberInfoDto);
        return scheduleMemberDto;
    }


    public void setMoimMemberInfoDto(MoimMemberInfoDto moimMemberInfoDto) {
        this.moimMemberInfoDto = moimMemberInfoDto;
    }

    public static ScheduleMemberDto create(ScheduleService.ChangeMemberTuple tuple) {
        ScheduleMemberDto scheduleMemberDto = new ScheduleMemberDto(tuple.getMemberScheduleLinker().getMemberState(),
                tuple.getMemberScheduleLinker().getCreatedAt(),
                tuple.getMemberScheduleLinker().getUpdatedAt());
        scheduleMemberDto.setMoimMemberInfoDto(tuple.getMoimMemberInfoDto());
        return scheduleMemberDto;
    }


}
