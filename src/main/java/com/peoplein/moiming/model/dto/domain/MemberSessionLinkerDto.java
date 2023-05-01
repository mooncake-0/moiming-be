package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.MemberSessionState;
import com.peoplein.moiming.domain.enums.SessionCategoryType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSessionLinkerDto {

    private Long memberId;
    private int singleCost;

    private List<SessionCategoryType> sessionCategoryTypes = new ArrayList<>();

    // MEMO:: 생성 요청시에는 없음, 전송시 필요 정보
    private MemberSessionState memberSessionState;
    private MoimMemberInfoDto moimMemberInfoDto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    /*
     Constructor -1
     응답용 Dto 형성시 필요 정보들을 토대로 Dto 형성
     */
    public MemberSessionLinkerDto(Long memberId, int singleCost, List<SessionCategoryType> sessionCategoryTypes, MoimMemberInfoDto moimMemberInfoDto
    , MemberSessionState memberSessionState, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.memberId = memberId;
        this.singleCost = singleCost;
        this.sessionCategoryTypes = sessionCategoryTypes;
        this.moimMemberInfoDto = moimMemberInfoDto;

        this.memberSessionState = memberSessionState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

}
