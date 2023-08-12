package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimMemberInfoDto {

    private Long memberId;
    private String memberName;
    private String memberEmail;
    private MemberGender memberGender;
    private MoimRoleType moimRoleType;
    private MoimMemberState moimMemberState;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MoimMemberInfoDto createMemberInfoDto(MemberMoimLinker memberMoimLinker) {
        return new MoimMemberInfoDto(
                memberMoimLinker.getMember().getId()
                , memberMoimLinker.getMember().getMemberInfo().getMemberName()
                , memberMoimLinker.getMember().getMemberEmail()
                , memberMoimLinker.getMember().getMemberInfo().getMemberGender()
                , memberMoimLinker.getMoimRoleType(), memberMoimLinker.getMemberState()
                , memberMoimLinker.getCreatedAt(), memberMoimLinker.getUpdatedAt());
    }

    /*
     Member 의 내용으로만 우선 형성될 수 있도록 따로 Open 한다
     */
    public MoimMemberInfoDto(Long memberId,  String memberName, String memberEmail, MemberGender memberGender) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.memberGender = memberGender;
    }

    public MoimMemberInfoDto(MemberMoimLinker memberMoimLinker) {
        this.memberId = memberMoimLinker.getMember().getId();
        this.memberName = memberMoimLinker.getMember().getMemberInfo().getMemberName();
        this.memberEmail = memberMoimLinker.getMember().getMemberEmail();
        this.memberGender = memberMoimLinker.getMember().getMemberInfo().getMemberGender();
        this.moimRoleType = memberMoimLinker.getMoimRoleType();
        this.moimMemberState = memberMoimLinker.getMemberState();
        this.createdAt = memberMoimLinker.getCreatedAt();

        if (updatedAt == null) {
            // 강퇴당한 경우 updateAt 생성되지 않음.
            this.updatedAt = LocalDateTime.now();
        } else {
            this.updatedAt = memberMoimLinker.getUpdatedAt();
        }
    }

    public static MoimMemberInfoDto createWithMemberMoimLinker(MemberMoimLinker memberMoimLinker) {
        return new MoimMemberInfoDto(
                memberMoimLinker.getMember().getId(),
                memberMoimLinker.getMember().getMemberInfo().getMemberName(),
                memberMoimLinker.getMember().getMemberEmail(),
                memberMoimLinker.getMember().getMemberInfo().getMemberGender(),
                memberMoimLinker.getMoimRoleType(), memberMoimLinker.getMemberState(),
                memberMoimLinker.getCreatedAt(), memberMoimLinker.getUpdatedAt());
    }

    /*
     MemberMoimLinker 정보 세팅을 위한 setter open
     */

    public void setMoimRoleType(MoimRoleType moimRoleType) {
        this.moimRoleType = moimRoleType;
    }

    public void setMoimMemberState(MoimMemberState moimMemberState) {
        this.moimMemberState = moimMemberState;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}