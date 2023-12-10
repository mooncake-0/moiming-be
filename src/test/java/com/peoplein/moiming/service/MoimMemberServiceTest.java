package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoimMemberServiceTest {

    @InjectMocks
    private MoimMemberService moimMemberService;

    @Mock
    private MoimRepository moimRepository;


    @Mock
    private MoimMemberRepository moimMemberRepository;


    // getMoimMembers Test
    // 1. 모임 못찾음
    @Test
    void getMoimMembers_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        // Wrapper 외 Test 객체에는 다 Mock 객체 필요함
        Member member = mock(Member.class);

        // given - stub
        when(moimRepository.findWithMoimMembersById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.getActiveMoimMembers(any(), member)).isInstanceOf(MoimingApiException.class);

    }


    // 2. getMoimMembers 호출성 확인 (moimId, curMember NN 보장 -> Validation / Security)
    @Test
    void getMoimMembers_shouldCallGetMethod_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findWithMoimMembersById(any())).thenReturn(Optional.ofNullable(moim));

        // when
        moimMemberService.getActiveMoimMembers(any(), member);

        // then
        verify(moim, times(1)).getMoimMembers();
    }


    // joinMoim Test
    // 모임 못찾음
    @Test
    void joinMoim_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        MoimMemberJoinReqDto requestDto = mock(MoimMemberJoinReqDto.class); // input 값을 딱히 활용하는게 없음. 큰 관심 없음
        Member member = mock(Member.class);

        // when
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> moimMemberService.joinMoim(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // Params 들은 NotNull 보장이므로 (Validation / Security Filter), 분기 경우가 없음
    // TODO: 사실 위의 경우도 다 TEST 해줘야함 (단위테스트는 앱의 흐름에 의존하면 안된다)
    // MoimMember 가 Null 이든, 실제 값이 있든, judgeMmeberJoinByRule 이 알아서 처리한다, 분기 경우 없음
    @Test
    void joinMoim_shouldCallJudgeMethod_whenRightInfoPassed() {

        // given
        MoimMemberJoinReqDto requestDto = mock(MoimMemberJoinReqDto.class);
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.ofNullable(moim));

        // when
        moimMemberService.joinMoim(requestDto, member);

        // then
        verify(moim, times(1)).judgeMemberJoinByRule(any(), any());
    }


    // leaveMoim
    // 성공 CASE
    @Test
    void leaveMoim_shouldLeaveMoim_whenRightInfoPassed() {

        // given
        MoimMemberLeaveReqDto requestDto = mock(MoimMemberLeaveReqDto.class);
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.ofNullable(moimMember));

        // when
        moimMemberService.leaveMoim(requestDto, member);

        // then
        verify(moimMember, times(1)).changeMemberState(any());

    }


    // 1 CASE - MoimMember 를 못찾음
    @Test
    void leaveMoim_shouldThrowException_whenNotFound_byMoimingApiException() {

        // given
        MoimMemberLeaveReqDto requestDto = mock(MoimMemberLeaveReqDto.class);
        Member member = mock(Member.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.leaveMoim(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 정상 통과
    @Test
    void expelMember_shouldCallMethods_whenRightInfoPassed() {

        // given
        MoimMemberExpelReqDto requestDto = mock(MoimMemberExpelReqDto.class);
        MoimMember requestMoimMember = mock(MoimMember.class);
        MoimMember expelMoimMember = mock(MoimMember.class);
        Member member = mock(Member.class);

        // given - stub1
        when(member.getId()).thenReturn(1L);
        when(requestDto.getExpelMemberId()).thenReturn(2L);


        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(eq(member.getId()), any())).thenReturn(Optional.ofNullable(requestMoimMember));
        when(requestMoimMember.hasPermissionOfManager()).thenReturn(true);
        when(moimMemberRepository.findByMemberAndMoimId(eq(requestDto.getExpelMemberId()), any())).thenReturn(Optional.ofNullable(expelMoimMember));

        // when
        moimMemberService.expelMember(requestDto, member);

        // then
        verify(expelMoimMember, times(1)).changeMemberState(any());
        verify(expelMoimMember, times(1)).setInactiveReason(any());

    }


    // expel MEMBER ERROR THROW
    // CASE 1 : 요청 멤버 정보 못찾음
    @Test
    void expelMember_shouldThrowExceptions_whenRequestMoimMemberNotFound_byMoimingApiException() {

        // given
        MoimMemberExpelReqDto requestDto = mock(MoimMemberExpelReqDto.class);
        Member member = mock(Member.class);

        // given - stub 1
        when(member.getId()).thenReturn(1L);
//        when(requestDto.getExpelMemberId()).thenReturn(2L);

        // given - stub 2
        when(moimMemberRepository.findByMemberAndMoimId(eq(member.getId()), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.expelMember(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // CASE 2 : 요청 멤버가 운영진이 아님
    @Test
    void expelMember_shouldThrowExceptions_whenRequestMemberNotManager_byMoimingApiException() {

        // given
        MoimMemberExpelReqDto requestDto = mock(MoimMemberExpelReqDto.class);
        MoimMember requestingMoimMember = mock(MoimMember.class);
        Member member = mock(Member.class);

        // given - stub 1
        when(member.getId()).thenReturn(1L);
//        when(requestDto.getExpelMemberId()).thenReturn(2L);

        // given - stub 2
        when(moimMemberRepository.findByMemberAndMoimId(eq(member.getId()), any())).thenReturn(Optional.ofNullable(requestingMoimMember));
        when(requestingMoimMember.hasPermissionOfManager()).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.expelMember(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // CASE 3 : 대상 멤버 정보 못찾음
    @Test
    void expelMember_shouldThrowExceptions_whenExpelMoimMemberNotFound_byMoimingApiException() {

        // given
        MoimMemberExpelReqDto requestDto = mock(MoimMemberExpelReqDto.class);
        MoimMember requestingMoimMember = mock(MoimMember.class);
        Member member = mock(Member.class);

        // given - stub 1
        when(member.getId()).thenReturn(1L);
        when(requestDto.getExpelMemberId()).thenReturn(2L);

        // given - stub 2
        when(moimMemberRepository.findByMemberAndMoimId(eq(member.getId()), any())).thenReturn(Optional.ofNullable(requestingMoimMember));
        when(requestingMoimMember.hasPermissionOfManager()).thenReturn(true);
        when(moimMemberRepository.findByMemberAndMoimId(eq(requestDto.getExpelMemberId()), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.expelMember(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // CASE 4 : 스스로 강퇴를 요청함
    @Test
    void expelMember_shouldThrowException_whenAttemptSelfExpel_byMoimingApiException() {

        // given
        MoimMemberExpelReqDto requestDto = mock(MoimMemberExpelReqDto.class);
        Member member = mock(Member.class);

        // given - stub 1
        when(member.getId()).thenReturn(1L);
        when(requestDto.getExpelMemberId()).thenReturn(1L);

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.expelMember(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }
}
