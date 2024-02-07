package com.peoplein.moiming.model.query;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
public class QueryMoimSuggestMapDto {

    /*
     불러오지 않은 PersistentBag 객체
     > MoimMembers 들, MoimCategoryLinkers 들은 호출하면 안됨
     */
    private Moim moim;
    private MoimJoinRule moimJoinRule;

    public QueryMoimSuggestMapDto(Object[] objects) {
        try {
            this.moim = (Moim) objects[0];
            if (this.moim.getMoimJoinRule() != null) {
                this.moimJoinRule = (MoimJoinRule) objects[1];
            }
        } catch (Exception exception) {
            log.error("{}, QueryMoimSuggestMapDto Constructor :: {}", this.getClass().getName(), "Query Dto 생성중 Cast Exception 혹은 Object [] 형성 문제 발생, " + objects.length);
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_SITUATION, exception);
        }
    }


    // Moim
//    private Long moimId;
//    private String moimName;
//    private int curMemberCount;
//    private int maxMember;
//    private Area moimArea;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private boolean hasJoinRule;

    // JoinRule
//    private boolean hasAgeRule;
//    private int ageMax;
//    private int ageMin;
//    private MemberGender memberGender;

    /*
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
     */

}
