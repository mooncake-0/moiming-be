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
     v1.1 회원가입 응답 정보 전달
    */
    @ApiModel(value = "Auth API - 응답 - 회원가입")
    @Getter
    @Setter
    public static class AuthSignUpRespDto {

        private Long memberId;
        private boolean hasMemberInfo;
        private String memberPhone;
        private String nickname;
        private String fcmToken;
        private String createdAt;
        private TokenRespDto tokenInfo;

        public AuthSignUpRespDto(Member member, TokenRespDto tokenRespDto) {
            this.memberId = member.getId();
            this.hasMemberInfo = false;
            if (member.getMemberInfo() != null) {
                this.hasMemberInfo = true;
            }
            this.memberPhone = member.getMemberPhone();
            this.nickname = member.getNickname();
            this.fcmToken = member.getFcmToken();
            this.createdAt = member.getCreatedAt() + "";
            this.tokenInfo = tokenRespDto;
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
        private boolean hasMemberInfo;
        private String memberPhone;
        private String nickname;
        private String fcmToken;
        private String createdAt;
        private String memberPfImgUrl;
        private TokenRespDto tokenInfo;

        // 로그인시 FCM Token 은 따로 참조가 필요하다
        public AuthLoginRespDto(Member member, TokenRespDto tokenRespDto, String fcmToken) {
            this.memberId = member.getId();
            this.hasMemberInfo = false;
            if (member.getMemberInfo() != null) {
                this.hasMemberInfo = true;
            }
            this.memberPhone = member.getMemberPhone();
            this.nickname = member.getNickname();
            this.fcmToken = fcmToken; // Member 그냥 정보만 담긴 객체라 fcm token 변경을 안함
            this.createdAt = member.getCreatedAt() + "";
            this.memberPfImgUrl = member.getPfImgUrl();
            this.tokenInfo = tokenRespDto;

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
