package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
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
    @AllArgsConstructor
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

        @Min(3)
        @Max(100)
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
        @AllArgsConstructor
        public static class JoinRuleCreateReqDto {

            @NotNull
            private boolean isAgeRule;

            @Max(100)
            private int ageMax;

            @Min(15)
            private int ageMin;

            @NotNull
            private MemberGender memberGender;

            // LOMBOK Getter 동장방식과 OM 의 동작방식 충돌 해결
            public void setIsAgeRule(boolean isAgeRule) {
                this.isAgeRule = isAgeRule;
            }
        }
    }


    /*
     PATCH 특성상 없는 필드들이 더 많을 것이므로 모두 다 NULLABLE
     */
    @ApiModel(value = "Moim API - 요청 - 모임 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimUpdateReqDto {

        @NotNull
        private Long moimId;

        @Size(min = 5, max = 50)
        private String moimName;

        @Size(min = 10, max = 5000)
        private String moimInfo;

        @Min(3)
        @Max(100)
        private Integer maxMember;

        private String areaCity;

        private String areaState;

        @Size(min = 2, max = 2, message = "카테고리는 수정될때도 부모/자식 두 개의 값이 들어와야 합니다")
        @JsonProperty("categories")
        private List<String> categoryNameValues; // 초기화를 막아두어야 아예 필드가 없는 경우도 통과한다

    }

}