package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.MoimMember;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

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
            private String memberPfImgUrl;

            public MoimMemberInfoDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                if (StringUtils.hasText(member.getMemberInfo().getPfImgUrl())) {
                    this.memberPfImgUrl = member.getMemberInfo().getPfImgUrl();
                }
            }
        }
    }
}
