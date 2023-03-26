package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.model.dto.SessionCategoryDetailsDto;
import com.peoplein.moiming.model.dto.domain.MemberSessionLinkerDto;
import com.peoplein.moiming.model.dto.domain.MoimSessionDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimSessionRequestDto {

    // Session 기본 정보
    private MoimSessionDto moimSessionDto;

    //
    // 정산 항목을 받아야 하고 * MoimSessionCategoryLinker
    // 정산 항목 내부에 하위 카테고리로 정산 품목들이 List 로 들어와있어야 한다
    private List<SessionCategoryDetailsDto> sessionCategoryDetailsDtos = new ArrayList<>();

    // 정산에 참여중인 멤버
    // 해당 정산에 참여하는 멤버가 있으면, 그 정보 안에 생성자도 있다면 동일한 기준으로 들어가 있다.
    // 해당 MemberSessionLinkerDto 에는 SessionCategory 가 List 로 같이 들어가 있을 것이다.
    private List<MemberSessionLinkerDto> memberSessionLinkerDtos = new ArrayList<>();

}
