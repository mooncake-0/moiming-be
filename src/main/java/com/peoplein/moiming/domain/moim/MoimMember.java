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

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;

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

        if (memberState.equals(ACTIVE)) {
            this.moim.addCurMemberCount();
        }
    }



    public void setMoimMemberRoleType(MoimMemberRoleType moimMemberRoleType) {
        this.moimMemberRoleType = moimMemberRoleType;
    }


    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }


    public void changeMemberState(MoimMemberState memberState) {
        // ACTIVE 외의 이동시 ACTIVE 에서 이동일 경우 minus 필요
        if (memberState.equals(ACTIVE)) {
            checkAndProcessRejoin();
        } else if (memberState.equals(IBW)) {
            checkAndProcessLeave();
        } else if (memberState.equals(IBF)) {
            checkAndProcessExpel();
        } else if (memberState.equals(DORMANT)) {
            checkAndProcessDormant();
        } else { // 회원탈퇴로 인한 변경
            checkAndProcessNotFound();
        }
        this.memberState = memberState;
    }


    private void checkAndProcessRejoin() { // IBW, DORMANT -> ACTIVE 가능, add 필요

        if (getMemberState().equals(IBW) || getMemberState().equals(DORMANT)) {
            if (moim.getCurMemberCount() + 1 > moim.getMaxMember()) {
                throw new MoimingApiException("모임 정원이 가득찼습니다");
            }

            this.moim.addCurMemberCount();

        } else {
            throw new MoimingApiException("재가입할 수 있는 대상이 아닙니다");
        }
    }

    private void checkAndProcessLeave() {// ACTIVE -> IBW 가능
        if (getMemberState().equals(ACTIVE)) {
            this.moim.minusCurMemberCount();
        }else{
            throw new MoimingApiException("모임을 나갈 수 있는 대상이 아닙니다");
        }
    }

    private void checkAndProcessExpel() {// ACTIVE, DORMANT -> IBF 가능
        if (getMemberState().equals(ACTIVE) || getMemberState().equals(DORMANT)) {
            if (getMemberState().equals(ACTIVE)) {
                this.moim.minusCurMemberCount();
            }
        }else{
            throw new MoimingApiException("모임에서 강퇴할 수 있는 대상이 아닙니다");
        }
    }

    private void checkAndProcessDormant() {// ACTIVE, IBW, IBF -> DORMANT 가능
        if(getMemberState().equals(DORMANT) || getMemberState().equals(NOTFOUND)){
            throw new MoimingApiException("휴면 상태로 전환될 수 있는 대상이 아닙니다");
        }else{
            if (getMemberState().equals(ACTIVE)) {
                this.moim.minusCurMemberCount();
            }
        }
    }


    private void checkAndProcessNotFound() {// ACTIVE, IBW, IBF, DORMANT -> NOTFOUND 가능
        if (getMemberState().equals(NOTFOUND)) {
            throw new MoimingApiException("탈퇴 상태로 전환될 수 있는 대상이 아닙니다");
        }else{ // 모두 전환 가능
            if (getMemberState().equals(ACTIVE)) {
                this.moim.minusCurMemberCount();
            }
        }
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