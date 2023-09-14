package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MoimMemberReqDto;
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
import static org.junit.jupiter.api.Assertions.*;
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
        assertThatThrownBy(() -> moimMemberService.getMoimMembers(any(), member)).isInstanceOf(MoimingApiException.class);

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
        moimMemberService.getMoimMembers(any(), member);

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


    // 1. MoimMember 를 못찾음
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
}
