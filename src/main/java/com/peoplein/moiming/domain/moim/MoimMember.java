package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Objects;

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;

@Entity
@Table(name = "moim_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moim_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @Enumerated(value = EnumType.STRING)
    private MoimMemberRoleType memberRoleType;

    @Enumerated(value = EnumType.STRING)
    private MoimMemberState memberState;

    private String inactiveReason;


    // 생성자
    public static MoimMember memberJoinMoim(Member member, Moim moim, MoimMemberRoleType memberRoleType, MoimMemberState memberState) {

        if (Objects.isNull(member) || Objects.isNull(moim) || Objects.isNull(memberRoleType) || Objects.isNull(memberState)) {
            throw new InvalidParameterException("Params 중 NULL 이 발생하였습니다");
        }

        if (moim.getCurMemberCount() + 1 > moim.getMaxMember()) {
            throw new MoimingApiException("모임 정원이 가득찼습니다");
        }
        MoimMember moimMember = new MoimMember(member, moim, memberRoleType, memberState);
        return moimMember;
    }

    private MoimMember(Member member, Moim moim, MoimMemberRoleType memberRoleType, MoimMemberState memberState) {

        this.member = member;
        this.memberRoleType = memberRoleType;
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


    public void changeMoimMemberRoleType(MoimMemberRoleType memberRoleType) {
        if (Objects.isNull(memberRoleType)) {
            throw new InvalidParameterException("Params 중 NULL 이 발생하였습니다");
        }

        if (getMemberState() != ACTIVE) {
            throw new MoimingApiException("활동중이지 않은 유저에게 운영진을 임명할 수 없습니다");
        }

        this.memberRoleType = memberRoleType;
    }


    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }



    /*
     해당 도메인이 원하는 로직의 여부를 수행한 후 반환
     > 예외처리를 진행할지 말지는 Service 의 역할로 둔다
    */

    public boolean hasPermissionOfManager() {
        return getMemberRoleType().equals(MoimMemberRoleType.MANAGER);
    }

    public boolean hasActivePermission() {
        return getMemberState().equals(ACTIVE);
    }

    public void changeMemberState(MoimMemberState memberState) {

        MoimMemberState curState = getMemberState();

        if (Objects.isNull(memberState)) {
            throw new InvalidParameterException("Params 중 NULL 이 발생하였습니다");
        }

        if (curState == memberState) {
            throw new MoimingApiException("같은 상태로의 전환 요청입니다");
        }

        if (curState == ACTIVE) { // 어떤 상태로도 변환이 가능하다
            this.moim.minusCurMemberCount();
        } else if (curState == IBW) {
            changeStateFromIBW(memberState);
        } else if (curState == IBF) {
            changeStateFromIBF(memberState);
        } else if (curState == IBD) {
            changeStateFromIBD(memberState);
        } else {
            throw new MoimingApiException("삭제된 계정의 연결 관계는 변경할 수 없습니다");
        }

        this.memberState = memberState;
    }


    private void changeStateFromIBW(MoimMemberState memberState) {
        if(memberState == ACTIVE){
            this.moim.addCurMemberCount();
        } else if (memberState == IBF) { // 불가능 CASE
            throw new MoimingApiException("불가능한 상태로 전환 요청입니다");
        }
    }

    private void changeStateFromIBF(MoimMemberState memberState) {
        if (memberState == ACTIVE) {
            throw new MoimingApiException("강퇴 유저는 재가입할 수 없습니다");
        } else if (memberState == IBW || memberState == IBD) {
            throw new MoimingApiException("불가능한 상태로 전환 요청입니다");
        }
    }

    private void changeStateFromIBD(MoimMemberState memberState) {
        if (memberState == ACTIVE) {
            this.moim.addCurMemberCount();
        } else if (memberState == IBW || memberState == IBF) {
            throw new MoimingApiException("불가능한 상태로 전환 요청입니다");
        }
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