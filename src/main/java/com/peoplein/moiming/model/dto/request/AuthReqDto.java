package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.PolicyType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class AuthReqDto {

    @ApiModel(value = "Login API - 요청 - 로그인")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthLoginReqDto {

        @NotEmpty
        private String memberEmail;

        @NotEmpty
        private String password;

    }


    @ApiModel(value = "Auth API - 요청 - 회원가입")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthSignInReqDto{

        @NotEmpty
        @Pattern(regexp = "^[a-zA-Z0-9]{1,30}@[a-z]{1,20}\\.[a-z]{1,10}$", message = "{1~30자리 영문 + 숫자}@{1~20자리 영문(소문자)}.{1~10자리 영문(소문자)}")
        private String memberEmail;

        @NotEmpty
        @Size(min = 4, max = 20, message = "4자~20자 (조건 Prod 시 추가 예정)")
        private String password;

        /*
         TODO :: 부가적 조건들 추가 예정
         */
        @NotEmpty
        @Size(max = 30, message = "이름은 최대 30자입니다")
        private String memberName;

        @NotEmpty
        @Size(max = 20, message = "번호는 최대 20자입니다")
        private String memberPhone;


        @NotNull
        private MemberGender memberGender;

        @NotNull
        private Boolean foreigner;


        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate memberBirth;

        @NotEmpty
        private String fcmToken;


        @NotEmpty
        @Size(max = 90, message = "CI 값은 88 byte 의 문자열입니다")
        private String ci;


        @NotEmpty // 안에 값이 들어있고, Null 이 아닌지도 체크
        @Size(min = 5, max = 5, message = "필요 항목은 5개입니다")
        @JsonProperty("policies")
        private List<PolicyAgreeDto> policyDtos;


        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PolicyAgreeDto{
            @NotNull
            private Boolean hasAgreed;

            @NotNull
            private PolicyType policyType;

        }
    }


    @ApiModel(value = "Auth API - 요청 - Token 갱신")
    @Getter
    @Setter
    public static class AuthTokenReqDto {

        @ApiModelProperty(value = "String 값 'REFRESH_TOKEN' 고정")
        @Pattern(regexp = "REFRESH_TOKEN", message = "String 값 'REFRESH_TOKEN' 고정")
        private String grantType;

        @NotEmpty
        private String token;

    }
}
