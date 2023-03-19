package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member_moim_linker")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMoimLinker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_moim_linker_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @Enumerated(value = EnumType.STRING)
    private MoimRoleType moimRoleType;

    @Enumerated(value = EnumType.STRING)
    private MoimMemberState memberState;

    private String inactiveReason;
    private boolean banRejoin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberMoimLinker memberJoinMoim(Member member, Moim moim, MoimRoleType moimRoleType, MoimMemberState memberState) {
        MemberMoimLinker memberMoimLinker = new MemberMoimLinker(member, moim, moimRoleType, memberState);
        return memberMoimLinker;
    }

    private MemberMoimLinker(Member member, Moim moim, MoimRoleType moimRoleType, MoimMemberState memberState) {

        DomainChecker.checkWrongObjectParams(this.getClass().getName(), member, moim, moimRoleType, memberState);

        this.member = member;
        this.moimRoleType = moimRoleType;
        this.memberState = memberState;

        /*
         초기화
         */
        this.createdAt = LocalDateTime.now();
        this.banRejoin = false;

        /*
         연관관계 매핑 및 편의 메소드
         */
        this.moim = moim;
        this.moim.getMemberMoimLinkers().add(this);
        this.moim.addCurMemberCount();
    }

    public void changeMemberState(MoimMemberState memberState) {
        if(!memberState.equals(MoimMemberState.ACTIVE)) {
            this.moim.minusCurMemberCount();
        }else{ // 기존에 INACTIVE 상태 쪽이던 회원이 다시 ACTIVE 하게 됨
            this.moim.addCurMemberCount();
        }
        // 이미 있던 모임 상태이므로
        this.memberState = memberState;
    }

    public void setMoimRoleType(MoimRoleType moimRoleType) {
        this.moimRoleType = moimRoleType;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

    public void setBanRejoin(boolean banRejoin) {

        this.banRejoin = banRejoin;
    }
}
