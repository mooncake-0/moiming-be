package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class MoimReqDto {

    @ApiModel(value = "Moim API - 요청 - 모임 생성")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimCreateReqDto {

        @NotEmpty
        @Size(min = 5, max = 30)
        private String moimName;

        @NotEmpty
        @Size(min = 10, max = 5000)
        private String moimInfo;

        @NotEmpty
        private String areaState;

        @NotEmpty
        private String areaCity;

        @Min(3) @Max(100)
        private int maxMember;

        @NotNull
        private boolean hasJoinRule;

        @JsonProperty("joinRule")
        private JoinRuleCreateReqDto joinRuleDto;

        @NotEmpty
        @JsonProperty("categories")
        private List<String> categoryNameValues = new ArrayList<>();

        public boolean hasJoinRule() {
            return this.hasJoinRule;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class JoinRuleCreateReqDto {

            @NotNull
            private boolean isAgeRule;

            @Max(100)
            private int ageMax;

            @Min(15)
            private int ageMin;

            @NotNull
            private MemberGender memberGender;

            public void setIsAgeRule(boolean isAgeRule) {
                this.isAgeRule = isAgeRule;
            }
        }
    }
}