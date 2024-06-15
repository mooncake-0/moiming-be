package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoimMemberServiceTest {

    @InjectMocks
    @Spy
    private MoimMemberService moimMemberService;

    @Mock
    private MoimRepository moimRepository;

    @Mock
    private MoimMemberRepository moimMemberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MemberRepository memberRepository;

    // getMoimMembers Test
    // 1. 모임원 반환
    @Test
    void getActiveMoimMembers_shouldPass_whenMoimMemberFound() {

        // given
        // Wrapper 외 Test 객체에는 다 Mock 객체 필요함
        Long moimId = 1L;
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimMemberRepository.findActiveWithMemberAndInfoByMoimId(any())).thenReturn(List.of(moimMember));

        // when
        // then
        assertDoesNotThrow(() -> moimMemberService.getActiveMoimMembers(moimId, member));
    }


    // 2. moimMember 를 아무도 찾지 못할 수가 없음
    //    moim 자체가 있는지의 CASE 와 구분이 안됨 - 일단 그냥 에러 보내지 않는걸로 한다
//    @Test
//    void getMoimMembers_shouldThrowException_whenNoMemberFound_byMoimingApiException() {
//
//        // given
//        Long moimId = 1L;
//
//        // given - stub
//        when(moimMemberRepository.findActiveWithMemberAndInfoByMoimId(any())).thenReturn(new ArrayList<>());
//
//        // when
//        // then
//        assertThatThrownBy(() -> moimMemberService.getActiveMoimMembers(moimId, any())).isInstanceOf(MoimingApiException.class);
//    }


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
        Member curManager = mock(Member.class);

        // given - stub
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.ofNullable(moim));
        when(memberRepository.findById(any())).thenReturn(Optional.of(curManager));

        // when
        moimMemberService.joinMoim(requestDto, member);

        // then - 어떤 값이 전달되는지 아무 신경쓰지 않음
        verify(moim, times(1)).judgeMemberJoinByRule(any(), any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    // leaveMoim
    // 성공 CASE
    @Test
    void leaveMoim_shouldLeaveMoim_whenNormalActiveMoimMemberReq() {

        // given
        MoimMemberLeaveReqDto requestDto = mock(MoimMemberLeaveReqDto.class);
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        Moim moim = mock(Moim.class); // Lazy Loading 으로 조회된다

        // given - stub
        when(moimMemberRepository.findWithMoimByMemberAndMoimId(any(), any())).thenReturn(Optional.ofNullable(moimMember));
        when(moimMember.getMoim()).thenReturn(moim);
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(moimMember.hasPermissionOfManager()).thenReturn(false);

        // when
        moimMemberService.leaveMoim(requestDto, member);

        // then
        verify(moimMember, times(1)).changeMemberState(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    // 1 CASE - MoimMember 를 못찾음
    @Test
    void leaveMoim_shouldThrowException_whenMoimMemberNotFound_byMoimingApiException() {

        // given
        MoimMemberLeaveReqDto requestDto = mock(MoimMemberLeaveReqDto.class);
        Member member = mock(Member.class);

        // given - stub
        when(moimMemberRepository.findWithMoimByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.leaveMoim(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 모임장이 나감을 시도하는데, 모임원이 남아있어서 승계를 해야함
    @Test
    void leaveMoim_shouldSuccessAndDelegateManager_whenManagerTrialButMoimMemberExists() {

        // given
        MoimMemberLeaveReqDto requestDto = mock(MoimMemberLeaveReqDto.class);
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        Moim moim = mock(Moim.class); // Lazy Loading 으로 조회된다

        // given - stub
        when(moimMemberRepository.findWithMoimByMemberAndMoimId(any(), any())).thenReturn(Optional.ofNullable(moimMember));
        when(moimMember.getMoim()).thenReturn(moim);
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(moimMember.hasPermissionOfManager()).thenReturn(true);
        when(moim.getCurMemberCount()).thenReturn(10);
        doNothing().when(moimMemberService).delegateMoimManager(any());

        // when
        moimMemberService.leaveMoim(requestDto, member);

        // then
        verify(moimMemberService, times(1)).delegateMoimManager(any());
        verify(moimMember, times(1)).changeMemberState(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    // 모임장이 나감을 시도하는데, 모임원이 없어서 모임이 삭제되는 상황
    @Test
    void leaveMoim_shouldSuccessAndMarkDeletion_whenManagerTrialAndNoMemberLeft() {

        // given
        MoimMemberLeaveReqDto requestDto = mock(MoimMemberLeaveReqDto.class);
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        Moim moim = mock(Moim.class); // Lazy Loading 으로 조회된다

        // given - stub
        when(moimMemberRepository.findWithMoimByMemberAndMoimId(any(), any())).thenReturn(Optional.ofNullable(moimMember));
        when(moimMember.getMoim()).thenReturn(moim);
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(moimMember.hasPermissionOfManager()).thenReturn(true);
        when(moim.getCurMemberCount()).thenReturn(1);

        // when
        moimMemberService.leaveMoim(requestDto, member);

        // then
        verify(moim, times(1)).markDeletedTime();
        verify(moimMemberService, times(0)).delegateMoimManager(any());
        verify(moimMember, times(1)).changeMemberState(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    // 정상 통과
    @Test
    void expelMember_shouldCallMethods_whenRightInfoPassed() {

        // given
        MoimMemberExpelReqDto requestDto = mock(MoimMemberExpelReqDto.class);
        MoimMember requestMoimMember = mock(MoimMember.class);
        Member member = mock(Member.class); // 요청한 멤버
        MoimMember expelMoimMember = mock(MoimMember.class);
        Member expelMember = mock(Member.class); // 강퇴 대상 멤버
        Moim moim = mock(Moim.class); // 대상 모임

        // given - stub1
        when(member.getId()).thenReturn(1L);
        when(requestDto.getExpelMemberId()).thenReturn(2L);
        when(expelMoimMember.getMoim()).thenReturn(moim);
        when(expelMoimMember.getMember()).thenReturn(expelMember);


        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(eq(member.getId()), any())).thenReturn(Optional.ofNullable(requestMoimMember));
        when(requestMoimMember.hasPermissionOfManager()).thenReturn(true);
        when(moimMemberRepository.findByMemberAndMoimId(eq(requestDto.getExpelMemberId()), any())).thenReturn(Optional.ofNullable(expelMoimMember));

        // when
        moimMemberService.expelMember(requestDto, member);

        // then
        verify(expelMoimMember, times(1)).changeMemberState(any());
        verify(expelMoimMember, times(1)).setInactiveReason(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

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


    // 오래된 멤버를 찾는데 실패함
    // 사실 불가능한 상황, 삭제된 모임에서만 발생할 상황인데, 삭제된 모임에선 요청이 불가능
    @Test
    void delegateMoimManager_shouldThrowException_whenLongestActiveMemberNotFound_byMoimingApiException() {

        // given
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimMemberRepository.findLongestNormalActiveMemberByMoimId(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimMemberService.delegateMoimManager(moim));

    }


    // 성공
    @Test
    void delegateMoimManager_shouldPass_whenRightInfoPassed() {

        // given
        Moim moim = mock(Moim.class);
        Member member = mock(Member.class);
        MoimMember longestMember = mock(MoimMember.class);

        // given - stub
        when(longestMember.getMember()).thenReturn(member);
        when(moimMemberRepository.findLongestNormalActiveMemberByMoimId(any())).thenReturn(Optional.of(longestMember));

        // when
        moimMemberService.delegateMoimManager(moim);

        // then
        verify(longestMember, times(1)).changeMoimMemberRoleType(MoimMemberRoleType.MANAGER);
        verify(moim, times(1)).changeCurManagerId(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


}
