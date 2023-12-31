package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

public class MemberRespDto {

    /*
     회원가입 응답 정보 전달
     */
    @ApiModel(value = "Auth API - 응답 - 회원가입")
    @Getter
    @Setter
    public static class MemberSignInRespDto {

        private Long id;
        private String memberEmail;
        private String nickname; // TODO :: 생성해줄 예정
        private String fcmToken;
        private String refreshToken;
        private String createdAt;
        private MemberInfoDto memberInfo;

        public MemberSignInRespDto(Member member) {
            this.id = member.getId();
            this.memberEmail = member.getMemberEmail();
            this.nickname = member.getNickname();
            this.fcmToken = member.getFcmToken();
            this.refreshToken = member.getRefreshToken();
            this.createdAt = member.getCreatedAt() + "";
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
                this.memberGender = memberInfo.getMemberGender().toString();
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
    public static class MemberLoginRespDto {
        private Long id;
        private String memberEmail;
        private String nickname;
        private String fcmToken;
        private String refreshToken;
        private String createdAt;
        private MemberInfoDto memberInfo;

        public MemberLoginRespDto(Member member) {
            this.id = member.getId();
            this.memberEmail = member.getMemberEmail();
            this.nickname = member.getNickname();
            this.fcmToken = member.getFcmToken();
            this.refreshToken = member.getRefreshToken();
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
                this.memberGender = memberInfo.getMemberGender().toString();
                this.memberBirth = memberInfo.getMemberBirth() + "";
                this.isForeigner = memberInfo.isForeigner();
            }
        }
    }
}
