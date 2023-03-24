package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestHelper;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberInfo;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoimMemberServiceTest {

    @Mock
    private Member creatorMember;
    @Mock
    private MemberInfo createrMemberInfo;
    @Mock
    private Member requestMember;
    @Mock
    private MemberInfo requestMemberInfo;
    @Mock
    private Moim curMoim;
    @Mock
    private MemberMoimLinker creatorMoimLinker;
    @Mock
    private RuleJoin curMoimRuleJoin;

    @Mock
    private MoimRepository moimRepository;
    @Mock
    private MemberMoimLinkerRepository memberMoimLinkerRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks // TEST 대상, @Mock 인 Bean 들은 이 곳에 주입된다
    private MoimMemberService moimMemberService;


    @BeforeEach
    void be() {
        creatorMember = mock(Member.class);
        createrMemberInfo = mock(MemberInfo.class);
        curMoim = mock(Moim.class);
        curMoimRuleJoin = mock(RuleJoin.class);
        creatorMoimLinker = mock(MemberMoimLinker.class);

        lenient().when(createrMemberInfo.getMemberName()).thenReturn("강우석");
        lenient().when(createrMemberInfo.getMemberBirth()).thenReturn(LocalDate.of(1995, 12, 18));
        lenient().when(createrMemberInfo.getMemberGender()).thenReturn(MemberGender.M);

        lenient().when(creatorMember.getId()).thenReturn(1L);
        lenient().when(creatorMember.getUid()).thenReturn("wrock.kang");
        lenient().when(creatorMember.getMemberInfo()).thenReturn(createrMemberInfo);

        // DEFAULT 조건들은 조건이 없는 것임
        lenient().when(curMoimRuleJoin.getBirthMax()).thenReturn(0);
        lenient().when(curMoimRuleJoin.getBirthMin()).thenReturn(0);
        lenient().when(curMoimRuleJoin.getGender()).thenReturn(MemberGender.N);
        lenient().when(curMoimRuleJoin.getMoimMaxCount()).thenReturn(0);
        lenient().when(curMoimRuleJoin.isDupLeaderAvailable()).thenReturn(true); // 중복이여도됨
        lenient().when(curMoimRuleJoin.isDupManagerAvailable()).thenReturn(true);

        lenient().when(curMoim.getId()).thenReturn(2L);
        lenient().when(curMoim.getMoimName()).thenReturn("모이밍");
        // MOIM RULE 관련 (조건이 현재 없지만, 있는 상황들을 Test 할 것이므로 있다고 세팅)
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        lenient().when(curMoim.getRuleJoin()).thenReturn(curMoimRuleJoin);
        String creatorMemberUid = creatorMember.getUid(); // mock return 값에 mock 필드 지정 금지
        lenient().when(curMoim.getCreatedUid()).thenReturn(creatorMemberUid);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(curMoimRuleJoin));

        // ML 관련
        lenient().when(creatorMoimLinker.getMember()).thenReturn(creatorMember);
        lenient().when(creatorMoimLinker.getMoim()).thenReturn(curMoim);
        lenient().when(creatorMoimLinker.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        lenient().when(creatorMoimLinker.getMoimRoleType()).thenReturn(MoimRoleType.CREATOR);
        List<MemberMoimLinker> memberMoimLinkers = new ArrayList<>();
        memberMoimLinkers.add(creatorMoimLinker);
        lenient().when(curMoim.getMemberMoimLinkers()).thenReturn(memberMoimLinkers);
        lenient().when(curMoim.getCurMemberCount()).thenReturn(1);

        // 요청자 정보
        requestMember = mock(Member.class);
        requestMemberInfo = mock(MemberInfo.class);
        lenient().when(requestMemberInfo.getMemberName()).thenReturn("박병호");
        lenient().when(requestMemberInfo.getMemberBirth()).thenReturn(LocalDate.of(1992, 7, 1));
        lenient().when(requestMemberInfo.getMemberGender()).thenReturn(MemberGender.M);

        lenient().when(requestMember.getId()).thenReturn(2L);
        lenient().when(requestMember.getUid()).thenReturn("bhpark1234");
        lenient().when(requestMember.getMemberInfo()).thenReturn(requestMemberInfo);
    }

    @Test
    @DisplayName("성공 @ viewMoimMember() 모든 회원 조회")
    void 모든_회원_상태_조회() {
        // given (SAMPLE) 멤버 두개 정도 추가 및 모임에 추가
        Member memberA = mock(Member.class);
        MemberInfo memberInfoA = mock(MemberInfo.class);

        lenient().when(memberInfoA.getMemberName()).thenReturn("김우진");
        lenient().when(memberInfoA.getMemberBirth()).thenReturn(LocalDate.of(1999, 7, 1));
        lenient().when(memberInfoA.getMemberGender()).thenReturn(MemberGender.M);
        lenient().when(memberA.getId()).thenReturn(3L);
        lenient().when(memberA.getUid()).thenReturn("kwj3591");
        lenient().when(memberA.getMemberInfo()).thenReturn(memberInfoA);

        List<MemberMoimLinker> sampleList = new ArrayList<>();
        MemberMoimLinker linker1 = mock(MemberMoimLinker.class);
        MemberMoimLinker linker2 = mock(MemberMoimLinker.class);
        lenient().when(linker1.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        lenient().when(linker1.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        lenient().when(linker1.getMoim()).thenReturn(curMoim);
        lenient().when(linker1.getMember()).thenReturn(requestMember); // 위에서 생성한 요청 유저가 이미 모임원임을 가정
        lenient().when(linker2.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        lenient().when(linker2.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        lenient().when(linker2.getMoim()).thenReturn(curMoim);
        lenient().when(linker2.getMember()).thenReturn(memberA);
        sampleList.add(linker1);
        sampleList.add(linker2);
        sampleList.add(creatorMoimLinker); // 생성자 (초기 유저)
        // 결과적인 모임 내 상황
        lenient().when(curMoim.getMemberMoimLinkers()).thenReturn(sampleList);
        // given - repo mock
        when(memberMoimLinkerRepository.findWithMemberInfoAndMoimByMoimId(curMoim.getId())).thenReturn(sampleList);

        // when 그 중 한명이 조회
        List<MoimMemberInfoDto> infos = moimMemberService.viewMoimMember(curMoim.getId(), requestMember);

        // then
        assertEquals(3, infos.size());
        infos.forEach(Assertions::assertNotNull);
    }
    @Test
    @DisplayName("성공 @ decideJoin() - ")
    void WAIT_유저_가입요청_판단() {

    }

    @Test
    @DisplayName("실패 @ decideJoin() - ")
    void WAIT_유저_가입요청_판단2() {

    }


    @Test
    @DisplayName("성공 @ exitMoim() - 자의로 나가기")
    void 자의로_모임_나가기() {

    }

    @Test
    @DisplayName("성공 @ exitMoim() - 타의로 나가기 - 인사 안함")
    void 인사_안해서_강퇴() {

    }

    @Test
    @DisplayName("성공 @ exitMoim() - 타의로 나가기 - 수칙 미준수")
    void 수칙_미준수_강퇴() {

    }

    @Test
    @DisplayName("성공 @ exitMoim() - 타의로 나가기 - 기타 사유 기록")
    void 기타_강퇴() {

    }

    @Test
    @DisplayName("성공 @ changeRole() - 역할 변경 성공")
    void 역할_변경() {

    }

    @Test
    @DisplayName("실패 @ changeRole() - 역할 변경 실패 ")
    void 역할_변경_실패() {
    }




}