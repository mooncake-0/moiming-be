package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

public class SearchRespDto {


    @ApiModel(value = "Search API - 응답 - 메인 모임 검색")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMoimRespDto {
        private Long moimId;
        private String moimName;
        private int curMemberCount;
        private int maxMember;
        private String areaCity;
        private String areaState;
        private String imgUrl;
        private String createdAt;
        private String updatedAt;
        @JsonProperty("joinRule")
        private MoimJoinRuleDto moimJoinRuleDto;
        private List<String> categories;

        public SearchMoimRespDto(Moim moim, List<MoimCategoryLinker> categoryLinkers) {
            this.moimId = moim.getId();
            this.moimName = moim.getMoimName();
            this.curMemberCount = moim.getCurMemberCount();
            this.maxMember = moim.getMaxMember();
            this.areaCity = moim.getMoimArea().getCity();
            this.areaState = moim.getMoimArea().getState();
            this.imgUrl = moim.getImgUrl();
            if (!StringUtils.hasText(moim.getImgUrl())) {
                this.imgUrl = AppParams.DEFAULT_MOIM_IMG_PATH;
            }
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
