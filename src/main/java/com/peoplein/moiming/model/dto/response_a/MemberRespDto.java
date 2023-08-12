package com.peoplein.moiming.model.dto.response_a;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.dto.domain.MemberRoleDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class MemberRespDto {

    @Getter
    @Setter
    public static class MemberSignInRespDto{

        private Long id;
        private String memberEmail;
        private String nickname; // TODO :: 생성해줄 예정
        private String fcmToken;
        private String memberName;
        private String createdAt;

        public MemberSignInRespDto(Member member) {
            this.id = member.getId();
            this.memberEmail = member.getMemberEmail();
//            this.nickname = member.getNickname();
            this.fcmToken = member.getFcmToken();
            this.memberName = member.getMemberInfo().getMemberName();
            this.createdAt = member.getMemberInfo().getCreatedAt() + ""; // 일시적, Date FORMAT 적용 필요
        }
    }
}
