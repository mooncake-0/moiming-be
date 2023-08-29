package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
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
    private MoimMemberRoleType moimMemberRoleType;
    private MoimMemberState moimMemberState;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MoimMemberInfoDto createMemberInfoDto(MoimMember moimMember) {
        return new MoimMemberInfoDto(
                moimMember.getMember().getId()
                , moimMember.getMember().getMemberInfo().getMemberName()
                , moimMember.getMember().getMemberEmail()
                , moimMember.getMember().getMemberInfo().getMemberGender()
                , moimMember.getMoimMemberRoleType(), moimMember.getMemberState()
                , moimMember.getCreatedAt(), moimMember.getUpdatedAt());
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

    public MoimMemberInfoDto(MoimMember moimMember) {
        this.memberId = moimMember.getMember().getId();
        this.memberName = moimMember.getMember().getMemberInfo().getMemberName();
        this.memberEmail = moimMember.getMember().getMemberEmail();
        this.memberGender = moimMember.getMember().getMemberInfo().getMemberGender();
        this.moimMemberRoleType = moimMember.getMoimMemberRoleType();
        this.moimMemberState = moimMember.getMemberState();
        this.createdAt = moimMember.getCreatedAt();

        if (updatedAt == null) {
            // 강퇴당한 경우 updateAt 생성되지 않음.
            this.updatedAt = LocalDateTime.now();
        } else {
            this.updatedAt = moimMember.getUpdatedAt();
        }
    }

    public static MoimMemberInfoDto createWithMemberMoimLinker(MoimMember moimMember) {
        return new MoimMemberInfoDto(
                moimMember.getMember().getId(),
                moimMember.getMember().getMemberInfo().getMemberName(),
                moimMember.getMember().getMemberEmail(),
                moimMember.getMember().getMemberInfo().getMemberGender(),
                moimMember.getMoimMemberRoleType(), moimMember.getMemberState(),
                moimMember.getCreatedAt(), moimMember.getUpdatedAt());
    }

    /*
     MoimMember 정보 세팅을 위한 setter open
     */

    public void setMoimMemberRoleType(MoimMemberRoleType moimMemberRoleType) {
        this.moimMemberRoleType = moimMemberRoleType;
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