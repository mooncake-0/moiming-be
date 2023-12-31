package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberDto {

    private Long id;
    private String memberEmail;
    private String nickname;
    private String fcmToken;
    private List<MemberRoleDto> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public MemberDto(Member member) {
        this.id = member.getId();
        this.memberEmail = member.getMemberEmail();
        this.nickname = member.getNickname();
        this.fcmToken = member.getFcmToken();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();

        convertLinkerToDto(member.getRoles());
    }

    /*
     MemberRole 에는 이미 Role 이 다 조회되어진 상태
     Dto 생성을 위해 전달, 추가 쿼리 없음
     */
    public void convertLinkerToDto(List<MemberRole> memberRoles) {
        this.roles = memberRoles.stream()
                .map(MemberRoleDto::new)
                .collect(Collectors.toList());
    }

}