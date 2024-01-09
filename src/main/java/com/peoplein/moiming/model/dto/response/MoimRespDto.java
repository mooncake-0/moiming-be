package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

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

        public MoimCreateRespDto(Moim moim){
            this.moimId = moim.getId();
            this.moimName = moim.getMoimName();
            this.moimInfo = moim.getMoimInfo();
            this.curMemberCount = moim.getCurMemberCount();
            this.maxMember = moim.getMaxMember();
            this.areaCity = moim.getMoimArea().getCity();
            this.areaState = moim.getMoimArea().getState();
            if (moim.getMoimJoinRule() != null) {
                this.joinRuleDto = new JoinRuleCreateRespDto(moim.getMoimJoinRule());
            }
            this.categoryNameValues = MoimCategoryLinker.convertLinkersToNameValues(moim.getMoimCategoryLinkers());
        }

        @Getter
        @Setter
        public static class JoinRuleCreateRespDto {
            private boolean hasAgeRule;
            private int ageMax;
            private int ageMin;
            private MemberGender memberGender;

            public JoinRuleCreateRespDto(MoimJoinRule moimJoinRule) {
                this.hasAgeRule = moimJoinRule.isHasAgeRule();
                this.ageMax = moimJoinRule.getAgeMax();
                this.ageMin = moimJoinRule.getAgeMin();
                this.memberGender = moimJoinRule.getMemberGender();
            }
        }
    }


    // 2. 모임 일반 정보 전달
    @ApiModel(value = "Moim API - 응답 - 유저 모임 일반 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimViewRespDto{

        private Long moimId;
        private String moimName;
        private int curMemberCount;
        private int maxMember;
        private String areaCity;
        private String areaState;
        private String createdAt;
        private String updatedAt;
        @JsonProperty("joinRule")
        private MoimJoinRuleDto moimJoinRuleDto;
        private List<String> categories;

        public MoimViewRespDto(MoimMember moimMember) {
            this.moimId = moimMember.getMoim().getId();
            this.moimName = moimMember.getMoim().getMoimName();
            this.curMemberCount = moimMember.getMoim().getCurMemberCount();
            this.maxMember = moimMember.getMoim().getMaxMember();
            this.areaCity = moimMember.getMoim().getMoimArea().getCity();
            this.areaState = moimMember.getMoim().getMoimArea().getState();
            this.createdAt = moimMember.getMoim().getCreatedAt() + "";
            this.updatedAt = moimMember.getMoim().getUpdatedAt() + "";
            this.categories = MoimCategoryLinker.convertLinkersToNameValues(moimMember.getMoim().getMoimCategoryLinkers());
            if (!Objects.isNull(moimMember.getMoim().getMoimJoinRule())) { // Join Rule 이 없는 모임일 수 있다
                this.moimJoinRuleDto = new MoimJoinRuleDto(moimMember.getMoim().getMoimJoinRule());
            }
        }


        @Getter
        @Setter
        @NoArgsConstructor
        public static class MoimJoinRuleDto {

            private boolean hasAgeRule;
            private int ageMax;
            private int ageMin;
            private MemberGender memberGender;

            public MoimJoinRuleDto(MoimJoinRule moimJoinRule) {
                this.hasAgeRule = moimJoinRule.isHasAgeRule();
                this.ageMax = moimJoinRule.getAgeMax();
                this.ageMin = moimJoinRule.getAgeMin();
                this.memberGender = moimJoinRule.getMemberGender();
            }
        }
    }


    // 3. 모임 세부 정보 전달 (User 정보들까지, 등등 정보)
    @ApiModel(value = "Moim API - 응답 - 모임 세부 조회 (모임 화면 이동 정보 전달)")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimDetailViewRespDto{

        private Long moimId;
        private String moimName;
        private String moimInfo;
        private int curMemberCount;
        private int maxMember;
        private String areaCity;
        private String areaState;

        // RULES 필요
        @JsonProperty("joinRule")
        private MoimJoinRuleDto moimJoinRuleDto;

        // 모든 MemberMoim 정보 전달 필요
        private List<String> categories;

        public MoimDetailViewRespDto(MoimMember moimMember) {

            this.moimJoinRuleDto = new MoimJoinRuleDto(moimMember.getMoim().getMoimJoinRule());
        }

        // 오히려 이게 필요 없을 수도 ?
        @Getter
        @Setter
        @NoArgsConstructor
        public static class MoimJoinRuleDto {

            private boolean hasAgeRule;
            private int ageMax;
            private int ageMin;
            private MemberGender memberGender;

            public MoimJoinRuleDto(MoimJoinRule moimJoinRule) {
                this.hasAgeRule = moimJoinRule.isHasAgeRule();
                this.ageMax = moimJoinRule.getAgeMax();
                this.ageMin = moimJoinRule.getAgeMin();
                this.memberGender = moimJoinRule.getMemberGender();
            }
        }
    }


    // 4. 모임 수정 후 정보 전달
    @ApiModel(value = "Moim API - 응답 - 모임 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimUpdateRespDto {

        private Long moimId;
        private String moimName;
        private String moimInfo;
        private int maxMember;
        private String areaState;
        private String areaCity;

        private List<String> categories;

        public MoimUpdateRespDto(Moim moim) {
            this.moimId = moim.getId();
            this.moimName = moim.getMoimName();
            this.moimInfo = moim.getMoimInfo();
            this.maxMember = moim.getMaxMember();
            this.areaState = moim.getMoimArea().getState();
            this.areaCity = moim.getMoimArea().getCity();
            this.categories = MoimCategoryLinker.convertLinkersToNameValues(moim.getMoimCategoryLinkers());
        }
    }
}
