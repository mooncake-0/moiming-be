package com.peoplein.moiming.model.query;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QueryMoimSuggestMapDto {

    // Moim
    private Long moimId;
    private String moimName;
    private int curMemberCount;
    private int maxMember;
    private Area moimArea;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasJoinRule;

    // JoinRule
    private boolean hasAgeRule;
    private int ageMax;
    private int ageMin;
    private MemberGender memberGender;

    public QueryMoimSuggestMapDto(Moim moim, MoimJoinRule joinRule) {
        this.moimId = moim.getId();
        this.moimName = moim.getMoimName();
        this.curMemberCount = moim.getCurMemberCount();
        this.maxMember = moim.getMaxMember();
        this.moimArea = moim.getMoimArea();
        this.createdAt = moim.getCreatedAt();
        this.updatedAt = moim.getUpdatedAt();
        this.hasJoinRule = moim.getMoimJoinRule() != null;
        if(hasJoinRule){
            this.hasAgeRule = joinRule.isHasAgeRule();
            this.ageMax = joinRule.getAgeMax();
            this.ageMin = joinRule.getAgeMin();
            this.memberGender = joinRule.getMemberGender();
        }

    }

}
