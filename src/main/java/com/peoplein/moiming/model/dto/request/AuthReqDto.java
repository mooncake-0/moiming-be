package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.VerificationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignUpReqDto.*;

public class AuthReqDto {

    @ApiModel(value = "Login API - 요청 - 로그인")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthLoginReqDto {

        @NotEmpty
        private String memberPhone;

        @NotEmpty
        private String password;

        @NotEmpty
        private String fcmToken;

    }


    @ApiModel(value = "Auth API - 요청 - 회원가입")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthSignUpReqDto{


        @NotNull
        private Long smsVerificationId; // 인증된 상태인 SMS Verification ID 를 같이 전달

        @NotEmpty
        @Size(max = 20, message = "번호는 최대 20자입니다")
        private String memberPhone;

        @NotEmpty
        @Size(min = 4, max = 20, message = "4자~20자 (조건 Prod 시 추가 예정)")
        private String password;

        @NotEmpty
        private String fcmToken;

        @NotEmpty // 안에 값이 들어있고, Null 이 아닌지도 체크
        @Size(min = 6, max = 6, message = "필요 항목은 6개입니다")
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


    @ApiModel(value = "Auth API - 요청 - 개발 테스트용 회원가입 (인증 불필요)")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DevAuthSignUnReqDto{


        @NotEmpty
        @Size(max = 20, message = "번호는 최대 20자입니다")
        private String memberPhone;

        @NotEmpty
        @Size(min = 4, max = 20, message = "4자~20자 (조건 Prod 시 추가 예정)")
        private String password;

        @NotEmpty
        private String fcmToken;

        @NotEmpty // 안에 값이 들어있고, Null 이 아닌지도 체크
        @Size(min = 6, max = 6, message = "필요 항목은 5개입니다")
        @JsonProperty("policies")
        private List<PolicyAgreeDto> policyDtos;

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


    @ApiModel(value = "Auth API - 요청 - 회원 EMAIL 찾기 (SMS 인증번호 및 Id 필요)")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthFindIdReqDto {
        @NotNull
        private Long smsVerificationId;
        @NotEmpty
        private String memberPhone;
        @NotEmpty
        private String verificationNumber;
    }


    @ApiModel(value = "Auth API - 요청 - 비밀번호 재설정 인증 요청 (SMS 인증번호 및 Id 필요)")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResetPwConfirmReqDto {
        @NotNull
        private Long smsVerificationId;
        @NotEmpty
        private String memberPhone;
        @NotEmpty
        private String verificationNumber;
    }


    @ApiModel(value = "Auth API - 요청 - 회원 가입 인증 요청 (SMS 인증번호 및 Id 필요)")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthSignUpSmsConfirmReqDto {
        @NotNull
        private Long smsVerificationId;
        @NotEmpty
        private String memberPhone;
        @NotEmpty
        private String verificationNumber;
    }


    @ApiModel(value = "Auth API - 요청 - 비밀번호 재설정 요청 (SMS Id 필요)")
    @Getter
    @Setter
    @AllArgsConstructor
    public static class AuthResetPwReqDto {
        @NotNull
        private Long smsVerificationId;
        @NotEmpty
        private String changePassword;
    }


    @ApiModel(value = "Auth SMS API - 요청 - SMS 인증번호 보내기")
    @Getter
    @Setter
    @AllArgsConstructor
    public static class AuthSmsReqDto {

        @NotNull
        private VerificationType verifyType;
        @NotEmpty
        private String memberPhone;
    }

}
