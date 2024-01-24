package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.MoimMember;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

public class MoimMemberRespDto {

    @ApiModel(value = "MoimMember API - 응답 - 모든 모임원 조회")
    @Getter
    @Setter
    public static class ActiveMoimMemberRespDto {

        private MoimMemberRoleType memberRoleType;
        private MoimMemberState memberState;
        private String createdAt;
        @JsonProperty("moimMemberInfo")
        private MoimMemberInfoDto memberDto;

        public ActiveMoimMemberRespDto(MoimMember moimMember) {
            this.memberRoleType = moimMember.getMemberRoleType();
            this.memberState = moimMember.getMemberState();
            this.createdAt = moimMember.getCreatedAt() + "";
            this.memberDto = new MoimMemberInfoDto(moimMember.getMember());
        }

        @Getter
        @Setter
        public static class MoimMemberInfoDto {

            private Long memberId;
            private String nickname;

            // TODO :: 사진 관련
            public MoimMemberInfoDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
            }
        }
    }
}
