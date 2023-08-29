package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class MoimRespDto {

    @ApiModel(value = "Moim API - 응답 - 모임 생성")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimCreateRespDto{

        private Long moimId;
        private String moimName;
        private String moimInfo;
        private int curMemberCount;
        private int maxMember;
        private String areaCity;
        private String areaState;
        @JsonProperty("joinRule")
        private JoinRuleCreateRespDto joinRuleDto;
        @JsonProperty("categories")
        private List<String> categoryNameValues;

        public MoimCreateRespDto(Moim moim, List<String> categoryNameValues){
            this.moimId = moim.getId();
            this.moimName = moim.getMoimName();
            this.moimInfo = moim.getMoimInfo();
            this.curMemberCount = moim.getCurMemberCount();
            this.maxMember = moim.getMaxMember();
            this.areaCity = moim.getMoimArea().getCity();
            this.areaState = moim.getMoimArea().getState();
            this.joinRuleDto = new JoinRuleCreateRespDto(moim.getMoimJoinRule());
            this.categoryNameValues = categoryNameValues;
        }

        @Getter
        @Setter
        public static class JoinRuleCreateRespDto {
            private boolean isAgeRule;
            private int ageMax;
            private int ageMin;
            private MemberGender memberGender;

            public JoinRuleCreateRespDto(MoimJoinRule moimJoinRule) {
                this.isAgeRule = moimJoinRule.isAgeRule();
                this.ageMax = moimJoinRule.getAgeMax();
                this.ageMin = moimJoinRule.getAgeMin();
                this.memberGender = moimJoinRule.getMemberGender();
            }
        }

    }
}
