package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimCountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoimCountServiceTest {


    @InjectMocks
    private MoimCountService moimCountService;

    @Mock
    private MoimCountRepository moimCountRepository;

    // 성공 - 유저의 오늘 첫 조회, 모임 기준 이번달 첫 조회
    @Test
    void processMoimCounting_shouldPass_whenUserFirstDailyMoimFirstMonthly() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimCountRepository.findDailyByMemberIdAndMoimIdAndCurrentDate(any(), any())).thenReturn(Optional.empty());
        when(moimCountRepository.findMonthlyByMemberIdAndMoimIdAndCurrentDate(any())).thenReturn(Optional.empty());

        // when
        moimCountService.processMoimCounting(member, moim);

        // then
        verify(moimCountRepository, times(1)).save((MoimDailyCount) any());
        verify(moimCountRepository, times(1)).save((MoimMonthlyCount) any());

    }


    // 성공 - 유저의 오늘 첫 조회, 모임 기준 이미 조회됨
    @Test
    void processMoimCounting_shouldPass_whenUserFirstDailyMoimAlreadyMonthly() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimMonthlyCount monthlyCount = mock(MoimMonthlyCount.class);

        // given - stub
        when(moimCountRepository.findDailyByMemberIdAndMoimIdAndCurrentDate(any(), any())).thenReturn(Optional.empty());
        when(moimCountRepository.findMonthlyByMemberIdAndMoimIdAndCurrentDate(any())).thenReturn(Optional.of(monthlyCount));

        // when
        moimCountService.processMoimCounting(member, moim);

        // then
        verify(moimCountRepository, times(1)).save((MoimDailyCount) any());
        verify(moimCountRepository, times(0)).save((MoimMonthlyCount) any());
        verify(monthlyCount, times(1)).increaseMonthlyCount();

    }


    // 성공 - 유저는 이미 조회함, 모임은 아무상관 없음 - 추가 쿼리가 나가지 않는 것도 확인
    @Test
    void processMoimCounting_shouldPass_whenUserAlreadyDailyMoimNoMatter() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimDailyCount dailyCount = mock(MoimDailyCount.class);

        // given - stub
        when(moimCountRepository.findDailyByMemberIdAndMoimIdAndCurrentDate(any(), any())).thenReturn(Optional.of(dailyCount));

        // when
        moimCountService.processMoimCounting(member, moim);

        // then
        verify(moimCountRepository, times(0)).save((MoimDailyCount) any());
        verify(moimCountRepository, times(0)).save((MoimMonthlyCount) any());
        verify(dailyCount, times(1)).increaseMemberAccessCount();

    }


    // 실패 NULL PARAMS

    @Test
    void processMoimCounting_shouldThrowException_whenParamsNull() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimCountService.processMoimCounting(null, null)).isInstanceOf(MoimingApiException.class);

    }
}