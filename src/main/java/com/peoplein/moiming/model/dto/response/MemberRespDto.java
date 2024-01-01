package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MemberRespDto {

    @ApiModel(value = "Member API - 응답 - 기본 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberViewRespDto{

        private Long memberId;
        private String memberEmail;
        private String nickname;
        private String createdAt;
        private String updatedAt;
        private String lastLoginAt;

        public MemberViewRespDto(Member member) {
            this.memberId = member.getId();
            this.memberEmail = member.getMemberEmail();
            this.nickname = member.getNickname();
            this.createdAt = member.getCreatedAt() + "";
            this.updatedAt = member.getUpdatedAt() + "";
            this.lastLoginAt = member.getLastLoginAt() + "";
        }

    }


    @ApiModel(value = "Member API - 응답 - 기본 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberDetailViewRespDto{

        private Long memberId;
        private String memberEmail;
        private String nickname;
        private String createdAt;
        private String updatedAt;
        private String lastLoginAt;
        @JsonProperty("memberInfo")
        private MemberInfoDto memberInfoDto;

        public MemberDetailViewRespDto(Member member) {
            this.memberId = member.getId();
            this.memberEmail = member.getMemberEmail();
            this.nickname = member.getNickname();
            this.createdAt = member.getCreatedAt() + "";
            this.updatedAt = member.getUpdatedAt() + "";
            this.lastLoginAt = member.getLastLoginAt() + "";
            this.memberInfoDto = new MemberInfoDto(member.getMemberInfo());
        }


        @Getter
        @Setter
        @NoArgsConstructor
        public static class MemberInfoDto {

            private String memberName;
            private String memberPhone;
            private MemberGender memberGender;
            private String memberBirth;
            private boolean foreigner;

            public MemberInfoDto(MemberInfo memberInfo) {
                this.memberName = memberInfo.getMemberName();
                this.memberPhone = memberInfo.getMemberPhone();
                this.memberGender = memberInfo.getMemberGender();
                this.memberBirth = memberInfo.getMemberBirth() + "";
                this.foreigner = memberInfo.isForeigner();
            }
        }
    }


    @ApiModel(value = "Member API - 응답 - 닉네임 변경")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberChangeNicknameRespDto{

        private String nickname;

    }
}
