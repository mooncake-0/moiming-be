package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AuthRespDto {

    /*
     회원가입 응답 정보 전달
     */
    @ApiModel(value = "Auth API - 응답 - 회원가입")
    @Getter
    @Setter
    public static class AuthSignInRespDto {

        private Long id;
        private String memberEmail;
        private String nickname; // TODO :: 생성해줄 예정
        private String fcmToken;
        private String createdAt;
        private TokenRespDto tokenInfo;
        private MemberInfoDto memberInfo;

        public AuthSignInRespDto(Member member, TokenRespDto tokenRespDto) {
            this.id = member.getId();
            this.memberEmail = member.getMemberEmail();
            this.nickname = member.getNickname();
            this.fcmToken = member.getFcmToken();
            this.createdAt = member.getCreatedAt() + "";
            this.tokenInfo = tokenRespDto;
            this.memberInfo = new MemberInfoDto(member.getMemberInfo());
        }

        /*
         포함 정보도 같을지라도 항상 DTO 분할 - 요청별로 묶음을 따로한다
         */
        @Getter
        @Setter
        public static class MemberInfoDto {

            private String memberName;
            private String memberPhone;
            private String memberGender;
            private String memberBirth;
            private boolean isForeigner;

            public MemberInfoDto(MemberInfo memberInfo) {
                this.memberName = memberInfo.getMemberName();
                this.memberPhone = memberInfo.getMemberPhone();
                this.memberGender = memberInfo.getMemberGender() + "";
                this.memberBirth = memberInfo.getMemberBirth() + "";
                this.isForeigner = memberInfo.isForeigner();
            }
        }
    }

    /*
     요청별로 DTO 를 분할 - 훨씬 깔끔한듯
     */
    @ApiModel(value = "Login API - 응답 - 로그인")
    @Getter
    @Setter
    public static class AuthLoginRespDto {

        private Long id;
        private String memberEmail;
        private String nickname;
        private String fcmToken;
        private String createdAt;
        private TokenRespDto tokenInfo;
        private MemberInfoDto memberInfo;

        public AuthLoginRespDto(Member member, TokenRespDto tokenRespDto) {
            this.id = member.getId();
            this.memberEmail = member.getMemberEmail();
            this.nickname = member.getNickname();
            this.fcmToken = member.getFcmToken();
            this.createdAt = member.getCreatedAt() + "";
            this.tokenInfo = tokenRespDto;
            this.memberInfo = new MemberInfoDto(member.getMemberInfo());

        }

        @Getter
        @Setter
        public static class MemberInfoDto {

            private String memberName;
            private String memberPhone;
            private String memberGender;
            private String memberBirth;
            private boolean isForeigner;

            public MemberInfoDto(MemberInfo memberInfo) {
                this.memberName = memberInfo.getMemberName();
                this.memberPhone = memberInfo.getMemberPhone();
                this.memberGender = memberInfo.getMemberGender() + "";
                this.memberBirth = memberInfo.getMemberBirth() + "";
                this.isForeigner = memberInfo.isForeigner();
            }
        }
    }


    @ApiModel(value = "Auth SMS API - 응답 - SMS 인증 요청 응답")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthSmsRespDto {

        private Long smsVerificationId;

        public AuthSmsRespDto(SmsVerification smsVerification) {
            this.smsVerificationId = smsVerification.getId();
        }
    }


    @ApiModel(value = "Auth API - 응답 - 이메일 찾기 응답")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthFindIdRespDto {

        private String maskedEmail;

    }
}
