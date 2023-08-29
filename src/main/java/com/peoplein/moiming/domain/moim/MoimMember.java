package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
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
public class MoimMember extends BaseEntity {

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
    private MoimMemberRoleType moimMemberRoleType;

    @Enumerated(value = EnumType.STRING)
    private MoimMemberState memberState;

    private String inactiveReason;
    private boolean banRejoin;

    // 생성자

    public static MoimMember memberJoinMoim(Member member, Moim moim, MoimMemberRoleType moimMemberRoleType, MoimMemberState memberState) {
        MoimMember moimMember = new MoimMember(member, moim, moimMemberRoleType, memberState);
        return moimMember;
    }

    private MoimMember(Member member, Moim moim, MoimMemberRoleType moimMemberRoleType, MoimMemberState memberState) {

        this.member = member;
        this.moimMemberRoleType = moimMemberRoleType;
        this.memberState = memberState;

        /*
         초기화
         */
        this.banRejoin = false;

        /*
         연관관계 매핑 및 편의 메소드
         */
        this.moim = moim;
        this.moim.getMoimMembers().add(this);

        if (memberState.equals(MoimMemberState.ACTIVE)) {
            this.moim.addCurMemberCount();
        }
    }

    public boolean shouldPersist() {
        return Objects.isNull(this.id);
    }


    public void setMoimMemberRoleType(MoimMemberRoleType moimMemberRoleType) {
        this.moimMemberRoleType = moimMemberRoleType;
    }


    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

    public void setBanRejoin(boolean banRejoin) {
        this.banRejoin = banRejoin;
    }

    public void upDateRoleTypeAndState(MoimMemberRoleType moimMemberRoleType, MoimMemberState memberState) {
        this.moimMemberRoleType = moimMemberRoleType;
        this.memberState = memberState;
    }

    public boolean canRejoin() {
        return banRejoin;
    }

    // 비즈니스 로직
    public static MoimMember processRequestJoin(Member curMember, Moim moim, MoimMemberState memberState, Optional<MoimMember> previousMemberMoimLinker) {
        if (moim.shouldCreateNewMemberMoimLinker(previousMemberMoimLinker)) {
            // 신규 가입하는 경우
            return MoimMember.memberJoinMoim(curMember, moim, MoimMemberRoleType.NORMAL, memberState);
        } else {
            // 탈퇴 후 재가입 하는 경우
            MoimMember moimMember = previousMemberMoimLinker.get();
            moimMember.upDateRoleTypeAndState(MoimMemberRoleType.NORMAL, memberState);
            return moimMember;
        }
    }

    public void changeMemberState(MoimMemberState memberState) {
        if (!memberState.equals(MoimMemberState.ACTIVE)) {
            this.moim.minusCurMemberCount();
        } else { // 기존에 INACTIVE 상태 쪽이던 회원이 다시 ACTIVE 하게 됨
            this.moim.addCurMemberCount();
        }
        // 이미 있던 모임 상태이므로
        this.memberState = memberState;
    }


    public void judgeJoin(MoimMemberStateAction stateAction) {
//        if (stateAction.equals(MoimMemberStateAction.PERMIT)) {
//            doActiveMemberState();
//        } else if (stateAction.equals(MoimMemberStateAction.DECLINE)) {
//            // TODO :: 이 멤버에게 [해당 모임에서 까였다고] 알림을 보내야 한다 (MEMBERINFO 를 JOIN 한 이유)
//            this.memberState = MoimMemberState.DECLINE;
//        } else { // 여기 들어오면 안되는 에러 요청
//            // TODO :: ERROR
//            throw new IllegalArgumentException("unexpected State");
//        }
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
        return this.moimMemberRoleType.equals(MoimMemberRoleType.MANAGER);
    }
}