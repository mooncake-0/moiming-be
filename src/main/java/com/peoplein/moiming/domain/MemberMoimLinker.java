package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.moim.Moim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "member_moim_linker")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMoimLinker extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_moim_linker_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @Enumerated(value = EnumType.STRING)
    private MoimRoleType moimRoleType;

    @Enumerated(value = EnumType.STRING)
    private MoimMemberState memberState;

    private String inactiveReason;
    private boolean banRejoin;

    // 생성자

    public static MemberMoimLinker memberJoinMoim(Member member, Moim moim, MoimRoleType moimRoleType, MoimMemberState memberState) {
        MemberMoimLinker memberMoimLinker = new MemberMoimLinker(member, moim, moimRoleType, memberState);
        return memberMoimLinker;
    }

    private MemberMoimLinker(Member member, Moim moim, MoimRoleType moimRoleType, MoimMemberState memberState) {

        this.member = member;
        this.moimRoleType = moimRoleType;
        this.memberState = memberState;

        /*
         초기화
         */
        this.banRejoin = false;

        /*
         연관관계 매핑 및 편의 메소드
         */
        this.moim = moim;
        this.moim.getMemberMoimLinkers().add(this);

        if (memberState.equals(MoimMemberState.ACTIVE)) {
            this.moim.addCurMemberCount();
        }
    }

    public boolean shouldPersist() {
        return Objects.isNull(this.id);
    }


    public void setMoimRoleType(MoimRoleType moimRoleType) {
        this.moimRoleType = moimRoleType;
    }


    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

    public void setBanRejoin(boolean banRejoin) {
        this.banRejoin = banRejoin;
    }

    public void upDateRoleTypeAndState(MoimRoleType moimRoleType, MoimMemberState memberState) {
        this.moimRoleType = moimRoleType;
        this.memberState = memberState;
    }

    public boolean canRejoin() {
        return banRejoin;
    }

    // 비즈니스 로직
    public static MemberMoimLinker processRequestJoin(Member curMember, Moim moim, MoimMemberState memberState, Optional<MemberMoimLinker> previousMemberMoimLinker) {
        if (moim.shouldCreateNewMemberMoimLinker(previousMemberMoimLinker)) {
            // 신규 가입하는 경우
            return MemberMoimLinker.memberJoinMoim(curMember, moim, MoimRoleType.NORMAL, memberState);
        } else {
            // 탈퇴 후 재가입 하는 경우
            MemberMoimLinker memberMoimLinker = previousMemberMoimLinker.get();
            memberMoimLinker.upDateRoleTypeAndState(MoimRoleType.NORMAL, memberState);
            return memberMoimLinker;
        }
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


    public void judgeJoin(MoimMemberStateAction stateAction) {
        if (stateAction.equals(MoimMemberStateAction.PERMIT)) {
            doActiveMemberState();
        } else if (stateAction.equals(MoimMemberStateAction.DECLINE)) {
            // TODO :: 이 멤버에게 [해당 모임에서 까였다고] 알림을 보내야 한다 (MEMBERINFO 를 JOIN 한 이유)
            this.memberState = MoimMemberState.DECLINE;
        } else { // 여기 들어오면 안되는 에러 요청
            // TODO :: ERROR
            throw new IllegalArgumentException("unexpected State");
        }
    }

    private void doActiveMemberState() {
        this.moim.addCurMemberCount();
        this.memberState = MoimMemberState.ACTIVE;
    }

    private void doInactiveMemberState(MoimMemberState moimMemberState) {
        this.moim.minusCurMemberCount();
        this.memberState = moimMemberState;
    }

    // 스케쥴 변경 권한은 LEADER / MANAGER만 있음.
    public boolean hasPermissionForUpdate() {
        return this.moimRoleType.equals(MoimRoleType.LEADER) ||
                this.moimRoleType.equals(MoimRoleType.MANAGER);
    }
}