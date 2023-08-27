package com.peoplein.moiming.model.dto.response_b;

import com.peoplein.moiming.model.dto.SessionCategoryDetailsDto;
import com.peoplein.moiming.model.dto.domain.MemberSessionLinkerDto;
import com.peoplein.moiming.model.dto.domain.MoimSessionDto;
import com.peoplein.moiming.model.dto.domain.ScheduleDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimSessionResponseDto {

    // MoimSession 기본 정보
    private MoimSessionDto moimSessionDto;

    // 없으면 Null 반환
    private ScheduleDto scheduleDto;

    // SessionItemCategory 정보
    private List<SessionCategoryDetailsDto> sessionCategoryDetailsDtos = new ArrayList<>();

    // MemberSessionLinker & MemberMoimInfoDto 정보
    private List<MemberSessionLinkerDto> memberSessionLinkerDtos = new ArrayList<>();

}
