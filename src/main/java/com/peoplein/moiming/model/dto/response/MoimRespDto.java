package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        private String createdAt;
        private String updatedAt;

        // RULES 필요
        @JsonProperty("joinRule")
        private MoimJoinRuleDto moimJoinRuleDto;

        // 모든 MemberMoim 정보 전달 필요
        private List<String> categories;

        @JsonProperty("creatorInfo")
        private MoimCreatorInfoDto creatorInfoDto;

        public MoimDetailViewRespDto(MoimMember moimMember) {
            this.moimId = moimMember.getMoim().getId();
            this.moimName = moimMember.getMoim().getMoimName();
            this.moimInfo = moimMember.getMoim().getMoimInfo();
            this.curMemberCount = moimMember.getMoim().getCurMemberCount();
            this.maxMember = moimMember.getMoim().getMaxMember();
            this.areaCity = moimMember.getMoim().getMoimArea().getCity();
            this.areaState = moimMember.getMoim().getMoimArea().getState();
            this.createdAt = moimMember.getMoim().getCreatedAt() + "";
            this.updatedAt = moimMember.getMoim().getUpdatedAt() + "";

            this.categories = MoimCategoryLinker.convertLinkersToNameValues(moimMember.getMoim().getMoimCategoryLinkers());
            this.creatorInfoDto = new MoimCreatorInfoDto(moimMember.getMember());
            if (moimMember.getMoim().getMoimJoinRule() != null) {
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

        @Getter
        @Setter
        @NoArgsConstructor
        public static class MoimCreatorInfoDto {

            // TODO :: 프로필 이미지

            private Long memberId;

            private String nickname;

            public MoimCreatorInfoDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
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


    @ApiModel(value = "Moim API - 응답 - 모임 가입 조건 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimJoinRuleUpdateRespDto{

        private boolean hasAgeRule;
        private int ageMax;
        private int ageMin;
        private MemberGender memberGender;

        public MoimJoinRuleUpdateRespDto(MoimJoinRule moimJoinRule) {
            this.hasAgeRule = moimJoinRule.isHasAgeRule();
            this.ageMax = moimJoinRule.getAgeMax();
            this.ageMin = moimJoinRule.getAgeMin();
            this.memberGender = moimJoinRule.getMemberGender();
        }
    }


    @ApiModel(value = "Moim API - 응답 - 모임 고정 정보 조회 (지역 / 카테고리)")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimFixedInfoRespDto {

        @JsonProperty("moimAreas")
        private List<MoimAreaDto> moimAreaDto;

        @JsonProperty("moimCategories")
        private List<MoimCategoryDto> moimCategoryDto;

        public MoimFixedInfoRespDto(List<AreaValue> areaState, List<Category> parentCategories, Map<Long, List<Category>> childCategoriesMap) {
            this.moimAreaDto = areaState.stream().map(MoimAreaDto::new).collect(Collectors.toList());
            this.moimCategoryDto = parentCategories.stream().map(parent -> new MoimCategoryDto(parent, childCategoriesMap.get(parent.getId()))).collect(Collectors.toList());

        }

        public static class MoimAreaDto {
            public String state;
            public List<String> cities;
            public MoimAreaDto(AreaValue areaState) {
                this.state = areaState.getName();
                this.cities = areaState.getStateCities().stream().map(AreaValue::getName).collect(Collectors.toList());
            }
        }

        public static class MoimCategoryDto {
            public String parentCategory;
            public List<String> childCategories;
            public MoimCategoryDto(Category parent, List<Category> categories) {
                this.parentCategory = parent.getCategoryName().getValue();
                this.childCategories = categories.stream().map(category -> category.getCategoryName().getValue()).collect(Collectors.toList());
            }
        }
    }


    @ApiModel(value = "Moim API - 응답 - 추천 모임 (이번 달 조회수가 가장 높은 모임들)")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimSuggestedDto {
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

        public MoimSuggestedDto(Moim moim, List<MoimCategoryLinker> categoryLinkers) {
            this.moimId = moim.getId();
            this.moimName = moim.getMoimName();
            this.curMemberCount = moim.getCurMemberCount();
            this.maxMember = moim.getMaxMember();
            this.areaCity = moim.getMoimArea().getCity();
            this.areaState = moim.getMoimArea().getState();
            this.createdAt = moim.getCreatedAt() + "";
            this.updatedAt = moim.getUpdatedAt() + "";
            this.categories = MoimCategoryLinker.convertLinkersToNameValues(categoryLinkers);
            if (!Objects.isNull(moim.getMoimJoinRule())) { // Join Rule 이 없는 모임일 수 있다
                this.moimJoinRuleDto = new MoimJoinRuleDto(moim.getMoimJoinRule());
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

}
