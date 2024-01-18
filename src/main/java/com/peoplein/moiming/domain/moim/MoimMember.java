package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Objects;

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.AUTH_REFRESH_TOKEN_NOT_MATCH;

@Slf4j
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
            log.error("Class {} : {}", "MoimMember.java", COMMON_INVALID_PARAM.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_PARAM);
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
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (getMemberState() != ACTIVE) {
            throw new MoimingApiException(MOIM_MEMBER_ROLE_GRANT_FAIL);
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
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (curState == memberState) { // 같은 상태로의 변경은 서버가 처리할 수 없음
            if (curState == ACTIVE) { // 이미 가입된 회원임
                throw new MoimingApiException(MOIM_JOIN_FAIL_BY_ALREADY_JOINED);
            }
            throw new MoimingApiException(MOIM_MEMBER_STATE_CHANGE_FAIL);
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
            throw new MoimingApiException(MOIM_MEMBER_STATE_CHANGE_FAIL);
        }

        this.memberState = memberState;
    }


    private void changeStateFromIBW(MoimMemberState memberState) {
        if(memberState == ACTIVE){
            this.moim.addCurMemberCount();
        } else if (memberState == IBF) { // 불가능 CASE
            throw new MoimingApiException(MOIM_MEMBER_STATE_CHANGE_FAIL);
        }
    }

    private void changeStateFromIBF(MoimMemberState memberState) {
        if (memberState == ACTIVE) {
            throw new MoimingApiException(MOIM_MEMBER_JOIN_FORBIDDEN);
        } else if (memberState == IBW || memberState == IBD) {
            throw new MoimingApiException(MOIM_MEMBER_STATE_CHANGE_FAIL);
        }
    }

    private void changeStateFromIBD(MoimMemberState memberState) {
        if (memberState == ACTIVE) {
            this.moim.addCurMemberCount();
        } else if (memberState == IBW || memberState == IBF) {
            throw new MoimingApiException(MOIM_MEMBER_STATE_CHANGE_FAIL);
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