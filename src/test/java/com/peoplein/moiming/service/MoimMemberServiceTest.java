package com.peoplein.moiming.service;

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
    @DisplayName("성공 @ requestJoin() - RuleJoin 이 없는 모임에 가입 요청")
    void 가입조건_없는_모임_가입요청() {

        //given (Rule Join 삭제)
        lenient().when(curMoim.getMoimRules()).thenReturn(new ArrayList<>());
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(false);
        lenient().when(curMoim.getRuleJoin()).thenReturn(null);
        // given (request 정보)
        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(curMoim.getId());
        when(moimRepository.findWithRulesById(curMoim.getId())).thenReturn(curMoim);

        //when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, requestMember);

        //then
        assertEquals(MoimRoleType.NORMAL, myMoimLinkerDto.getMoimRoleType());
        assertEquals(MoimMemberState.ACTIVE, myMoimLinkerDto.getMemberState());
        assertNotNull(myMoimLinkerDto.getCreatedAt());

        // MEMO :: 당연히 안되는 것이긴 함. 여기서 Moim 은 그대로기 때문
        //          Service Tranx 내의 Moim 도 mock 객체이기 때문에 addCurMemberCount 등의 함수를 수행하지 않음.
        //          그렇다면 이런 연관관계 메소드 테스트는 어디에서? 실제 DB를 거치면서 EntityManager가 역할을 수행하는 모습을 봐야할 듯
        //          Repository Test 랑도 다르고, Domain Test 랑도 좀 다른 것 같음
        //          일단 여기서는 Service 에서 수행하는 로직이 잘 수행되는가 결과만 확인하면 되는 것이기 때문에 이런 방식으로 충분할 듯.
//        assertEquals(2, curMoim.getCurMemberCount());
//        assertEquals(2, curMoim.getMemberMoimLinkers().size());
    }

    @Test
    @DisplayName("성공 @ requestJoin() - 모든 RuleJoin 조건에 충족하는 모임에 가입 요청")
    void 모든_조건_충족_가입요청() {

        // given (Rule Join 이 존재하도록 설정)
        when(curMoimRuleJoin.getBirthMax()).thenReturn(2000);
        when(curMoimRuleJoin.getBirthMin()).thenReturn(1990);
        when(curMoimRuleJoin.getGender()).thenReturn(MemberGender.M);
        when(curMoimRuleJoin.getMoimMaxCount()).thenReturn(2);
        when(curMoimRuleJoin.isDupLeaderAvailable()).thenReturn(false);
        when(curMoimRuleJoin.isDupManagerAvailable()).thenReturn(true);

        // given - SAMPLE MOIM LINKERS
        List<MemberMoimLinker> sampleList = new ArrayList<>();
        MemberMoimLinker linker1 = mock(MemberMoimLinker.class);
        MemberMoimLinker linker2 = mock(MemberMoimLinker.class);
        when(linker1.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        when(linker1.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        when(linker2.getMoimRoleType()).thenReturn(MoimRoleType.MANAGER); // 매니저 겸직 허용 충족
        when(linker2.getMemberState()).thenReturn(MoimMemberState.IBF);
        sampleList.add(linker1);
        sampleList.add(linker2);

        // given - 사용 repository 함수 지정
        when(moimRepository.findWithRulesById(curMoim.getId())).thenReturn(curMoim);
        when(memberMoimLinkerRepository.findByMemberId(requestMember.getId())).thenReturn(sampleList);
        when(memberRepository.findMemberAndMemberInfoById(requestMember.getId())).thenReturn(requestMember);

        // given (request 정보)
        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(curMoim.getId());

        // when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, requestMember);

        // then
        assertEquals(MoimRoleType.NORMAL, myMoimLinkerDto.getMoimRoleType());
        assertEquals(MoimMemberState.ACTIVE, myMoimLinkerDto.getMemberState());
        assertNotNull(myMoimLinkerDto.getCreatedAt());
    }

    @Test
    @DisplayName("실패 @ requestJoin() - 나이 초과로 가입 요청 대기")
    void 나이초과_가입요청() {

        // given (Rule Join 이 존재하도록 설정) 나이가 많아서일수도 적어서일수도
//        when(curMoimRuleJoin.getBirthMax()).thenReturn(2000);
//        when(curMoimRuleJoin.getBirthMin()).thenReturn(1995);
        when(curMoimRuleJoin.getBirthMax()).thenReturn(1990);
        when(curMoimRuleJoin.getBirthMin()).thenReturn(1985);

        // given - 사용 repository 함수 지정
        lenient().when(moimRepository.findWithRulesById(curMoim.getId())).thenReturn(curMoim);
        lenient().when(memberRepository.findMemberAndMemberInfoById(requestMember.getId())).thenReturn(requestMember);

        // given (request 정보)
        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(curMoim.getId());

        // when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, requestMember);

        // then
        assertEquals(MoimRoleType.NORMAL, myMoimLinkerDto.getMoimRoleType());
        assertEquals(MoimMemberState.WAIT_BY_AGE, myMoimLinkerDto.getMemberState());
        assertNotNull(myMoimLinkerDto.getCreatedAt());
    }


    @Test
    @DisplayName("실패 @ requestJoin() - 요구 성별 불일치로 가입 요청 대기")
    void 성별불일치_가입요청() {

        // given (Rule Join 이 존재하도록 설정)
        when(curMoimRuleJoin.getGender()).thenReturn(MemberGender.F);

        // given - 사용 repository 함수 지정
        when(moimRepository.findWithRulesById(curMoim.getId())).thenReturn(curMoim);
        when(memberRepository.findMemberAndMemberInfoById(requestMember.getId())).thenReturn(requestMember);

        // given (request 정보)
        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(curMoim.getId());

        // when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, requestMember);

        // then
        assertEquals(MoimRoleType.NORMAL, myMoimLinkerDto.getMoimRoleType());
        assertEquals(MoimMemberState.WAIT_BY_GENDER, myMoimLinkerDto.getMemberState());
        assertNotNull(myMoimLinkerDto.getCreatedAt());
    }

    @Test
    @DisplayName("실패 @ requestJoin() - 가입 모임수 초과로 가입 요청 대기")
    void 가입_모임수_초과_가입요청() {

        // given (Rule Join 이 존재하도록 설정)
        when(curMoimRuleJoin.getMoimMaxCount()).thenReturn(2);

        // given - SAMPLE MOIM LINKERS
        List<MemberMoimLinker> sampleList = new ArrayList<>();
        MemberMoimLinker linker1 = mock(MemberMoimLinker.class);
        MemberMoimLinker linker2 = mock(MemberMoimLinker.class);
        MemberMoimLinker linker3 = mock(MemberMoimLinker.class);
        when(linker1.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        when(linker1.getMemberState()).thenReturn(MoimMemberState.WAIT);
        when(linker2.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        when(linker2.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        when(linker3.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        when(linker3.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        sampleList.add(linker1);
        sampleList.add(linker2);
        sampleList.add(linker3);

        // given - 사용 repository 함수 지정
        when(moimRepository.findWithRulesById(curMoim.getId())).thenReturn(curMoim);
        when(memberMoimLinkerRepository.findByMemberId(requestMember.getId())).thenReturn(sampleList);
        when(memberRepository.findMemberAndMemberInfoById(requestMember.getId())).thenReturn(requestMember);

        // given (request 정보)
        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(curMoim.getId());

        // when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, requestMember);

        // then
        assertEquals(MoimRoleType.NORMAL, myMoimLinkerDto.getMoimRoleType());
        assertEquals(MoimMemberState.WAIT_BY_MOIM_CNT, myMoimLinkerDto.getMemberState());
        assertNotNull(myMoimLinkerDto.getCreatedAt());
    }

    @Test
    @DisplayName("실패 @ requestJoin() - 겸직으로 가입 요청 대기")
    void 겸직_가입요청() {

        // given (Rule Join 이 존재하도록 설정)
        lenient().when(curMoimRuleJoin.isDupLeaderAvailable()).thenReturn(false);
        lenient().when(curMoimRuleJoin.isDupManagerAvailable()).thenReturn(false);

        List<MemberMoimLinker> sampleList = new ArrayList<>();
        MemberMoimLinker linker1 = mock(MemberMoimLinker.class);
        MemberMoimLinker linker2 = mock(MemberMoimLinker.class);
        when(linker1.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        when(linker1.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        when(linker2.getMoimRoleType()).thenReturn(MoimRoleType.LEADER); // 리더인 모임이 있음
        when(linker2.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        sampleList.add(linker1);
        sampleList.add(linker2);

        // given - 사용 repository 함수 지정
        when(moimRepository.findWithRulesById(curMoim.getId())).thenReturn(curMoim);
        when(memberMoimLinkerRepository.findByMemberId(requestMember.getId())).thenReturn(sampleList);
        when(memberRepository.findMemberAndMemberInfoById(requestMember.getId())).thenReturn(requestMember);

        // given (request 정보)
        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(curMoim.getId());

        // when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, requestMember);

        // then
        assertEquals(MoimRoleType.NORMAL, myMoimLinkerDto.getMoimRoleType());
        assertEquals(MoimMemberState.WAIT_BY_DUP, myMoimLinkerDto.getMemberState());
        assertNotNull(myMoimLinkerDto.getCreatedAt());
    }

    @Test
    @DisplayName("성공 @ requestJoin() - 재가입 시도")
    void 재가입_가입요청() {


    }

    @Test
    @DisplayName("실패 @ requestJoin() - 재가입 금지로 인한 실패")
    void 재가입_금지_가입요청() {

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