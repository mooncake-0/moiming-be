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

        @JsonProperty("joinRule")
        private JoinRuleCreateReqDto joinRuleDto;

        private Long imgFileId;

        @NotEmpty
        @Size(min = 2, max = 2)
        @JsonProperty("categories")
        private List<String> categoryNameValues = new ArrayList<>();


        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class JoinRuleCreateReqDto {

            @NotNull(message = "빈 값 금지, ageRule=false 시 -1 로 전달")
            @Min(value = 1000, message = "v0.1.5 - 나이값이 아닌 년생으로 변경")
            private Integer ageMax;

            @NotNull(message = "빈 값 금지, ageRule=false 시 -1 로 전달")
            @Min(value = 1000, message = "v0.1.5 - 나이값이 아닌 년생으로 변경")
            private Integer ageMin;

            @NotNull
            private MemberGender memberGender;

        }
    }


    /*
     PATCH 특성상 없는 필드들이 더 많을 것이므로 모두 다 NULLABLE
     */
    @ApiModel(value = "Moim API - 요청 - 모임 정보 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimUpdateReqDto {

        @NotNull
        private Long moimId;

        @Size(min = 5, max = 50)
        private String moimName;

        @Size(min = 10, max = 5000)
        private String moimInfo;

        private String areaState;

        private String areaCity;

        @Size(min = 2, max = 2, message = "카테고리는 수정될때도 부모/자식 두 개의 값이 들어와야 합니다")
        @JsonProperty("categories")
        private List<String> categoryNameValues; // 초기화를 막아두어야 아예 필드가 없는 경우도 통과한다

    }


    /*
     화면상 한번에 정보를 보내줄 것이기 때문에 전부 다 받는다
     */
    @ApiModel(value = "Moim API - 요청 - 모임 가입 조건 및 최대 인원 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimJoinRuleUpdateReqDto {

        @NotNull
        private Long moimId;

        @NotNull
        private Boolean editingJoinRule;

        @Min(3)
        @Max(100)
        private Integer maxMember;

        @Min(value = 1000, message = "v0.1.5 - 나이값이 아닌 년생으로 변경")
        private Integer ageMax;

        @Min(value = 1000, message = "v0.1.5 - 나이값이 아닌 년생으로 변경")
        private Integer ageMin;

        private MemberGender memberGender;

    }

}