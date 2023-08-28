package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MoimReqDto {


    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimCreateReqDto {

        private String moimName;
        private String moimInfo;
        private String areaState;
        private String areaCity;
        private int maxMember;
        private boolean hasJoinRule;
        @JsonProperty("joinRule")
        private JoinRuleCreateReqDto joinRuleDto;
        @JsonProperty("categories")
        private List<String> categoryNameValues = new ArrayList<>();

        public boolean hasJoinRule() {
            return this.hasJoinRule;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class JoinRuleCreateReqDto {

            private boolean isAgeRule;
            private int ageMax;
            private int ageMin;
            private MemberGender memberGender;

        }
    }
}