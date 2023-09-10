package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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


    // 생성자
    public static MoimMember memberJoinMoim(Member member, Moim moim, MoimMemberRoleType moimMemberRoleType, MoimMemberState memberState) {
        if (moim.getCurMemberCount() + 1 > moim.getMaxMember()) {
            throw new MoimingApiException("모임 정원이 가득찼습니다");
        }
        MoimMember moimMember = new MoimMember(member, moim, moimMemberRoleType, memberState);
        return moimMember;
    }

    private MoimMember(Member member, Moim moim, MoimMemberRoleType moimMemberRoleType, MoimMemberState memberState) {

        this.member = member;
        this.moimMemberRoleType = moimMemberRoleType;
        this.memberState = memberState;


        /*
         연관관계 매핑 및 편의 메소드
         */
        this.moim = moim;
        this.moim.getMoimMembers().add(this);

        if (memberState.equals(MoimMemberState.ACTIVE)) {
            this.moim.addCurMemberCount();
        }
    }



    public void setMoimMemberRoleType(MoimMemberRoleType moimMemberRoleType) {
        this.moimMemberRoleType = moimMemberRoleType;
    }


    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }



    public void checkRejoinAvailable() {
        if (getMemberState().equals(MoimMemberState.IBF)) {
            throw new MoimingApiException("강퇴 유저는 재가입할 수 없습니다");
        }

        if (!getMemberState().equals(MoimMemberState.ACTIVE)) {
            throw new MoimingApiException("가입할 수 있는 유저가 아닙니다 (시스템 오류)");
        }
    }


    public void changeMemberState(MoimMemberState memberState) {
        if (!memberState.equals(MoimMemberState.ACTIVE)) {
            this.moim.minusCurMemberCount();

        } else { // ACTIVE 하지 않던 유저를 다시 ACTIVE 하게 하려고 한다

            if (moim.getCurMemberCount() + 1 > moim.getMaxMember()) {
                throw new MoimingApiException("모임 정원이 가득찼습니다");
            }

            this.moim.addCurMemberCount();
        }

        // 이미 있던 모임 상태이므로
        this.memberState = memberState;
    }



    public boolean hasPermissionOfManager() {
        return this.moimMemberRoleType.equals(MoimMemberRoleType.MANAGER);
    }


    // WARN: ID 변경은 MOCK 용: 호출된 곳이 test Pckg 인지 확인
    public void changeMockObjectIdForTest(Long mockObjectId, URL classUrl) {

        try {
            URI uri = classUrl.toURI();
            File file = new File(uri);
            String absolutePath = file.getAbsolutePath();

            if (absolutePath.contains("test")) { // 빌드 Class 경로가 test 내부일경우
                this.id = mockObjectId;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}