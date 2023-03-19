package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.model.dto.domain.ScheduleDto;
import com.peoplein.moiming.model.dto.domain.ScheduleMemberDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleResponseDto {

    private ScheduleDto scheduleDto;
    private List<ScheduleMemberDto> scheduleMemberDto = new ArrayList<>();

    public ScheduleResponseDto(ScheduleDto scheduleDto) {
        this.scheduleDto = scheduleDto;
    }

    public void setScheduleMemberDto(List<ScheduleMemberDto> scheduleMemberDto) {
        this.scheduleMemberDto = scheduleMemberDto;
    }
}
