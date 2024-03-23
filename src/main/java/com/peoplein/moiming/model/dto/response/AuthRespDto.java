package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static com.peoplein.moiming.config.AppParams.*;

public class AuthRespDto {

    /*
     회원가입 응답 정보 전달
     */
    @ApiModel(value = "Auth API - 응답 - 회원가입")
    @Getter
    @Setter
    public static class AuthSignInRespDto {

        private Long memberId;
        private String memberEmail;
        private String nickname; // TODO :: 생성해줄 예정
        private String fcmToken;
        private String createdAt;
        private TokenRespDto tokenInfo;
        private MemberInfoDto memberInfo;

        public AuthSignInRespDto(Member member, TokenRespDto tokenRespDto) {
            this.memberId = member.getId();
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
            private String memberPfImgUrl;

            public MemberInfoDto(MemberInfo memberInfo) {
                this.memberName = memberInfo.getMemberName();
                this.memberPhone = memberInfo.getMemberPhone();
                this.memberGender = memberInfo.getMemberGender() + "";
                this.memberBirth = memberInfo.getMemberBirth() + "";
                this.memberPfImgUrl = DEFAULT_MEMBER_PF_IMG_PATH; // 회원가입시는 DEFAULT
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

        private Long memberId;
        private String memberEmail;
        private String nickname;
        private String fcmToken;
        private String createdAt;
        private TokenRespDto tokenInfo;
        private MemberInfoDto memberInfo;

        public AuthLoginRespDto(Member member, TokenRespDto tokenRespDto) {
            this.memberId = member.getId();
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
            private String memberPfImgUrl;

            public MemberInfoDto(MemberInfo memberInfo) {
                this.memberName = memberInfo.getMemberName();
                this.memberPhone = memberInfo.getMemberPhone();
                this.memberGender = memberInfo.getMemberGender() + "";
                this.memberBirth = memberInfo.getMemberBirth() + "";
                this.memberPfImgUrl = DEFAULT_MEMBER_PF_IMG_PATH;
                if (StringUtils.hasText(memberInfo.getPfImgUrl())) {
                    this.memberPfImgUrl = memberInfo.getPfImgUrl();
                }
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
