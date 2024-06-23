package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberSetting;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MemberSettingReqDto;
import com.peoplein.moiming.repository.jpa.MemberSettingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MemberSettingReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberSettingServiceTest {

    @InjectMocks
    private MemberSettingService memberSettingService;

    @Mock
    private MemberSettingRepository memberSettingRepository;


    // NOTICE 통과 / 실패
    // Member Setting 못찾음 -> 굳이 예외까진 던지지 않음
    @Test
    void isMemberNotifyAllowed_shouldReturnFalse_whenMemberSettingNotFound() {

        // given
        Member receiver = mock(Member.class);
        NotificationTopCategory topCategory = mock(NotificationTopCategory.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.empty());

        // when
        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);

        // then
        assertFalse(result);

    }


    // isMemberFcmAllowed -> MOIM 알림일 경우, 허요된 경우
    @Test
    void isMemberNotifyAllowed_shouldReturnTrue_whenMoimActivityNotifyAllowed() {

        // given
        Member receiver = mock(Member.class);
        NotificationTopCategory topCategory = NotificationTopCategory.MOIM;
        MemberSetting memberSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
        when(memberSetting.isMoimActivityNotifyAllowed()).thenReturn(true);

        // when
        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);

        // then
        assertTrue(result);
        verify(memberSetting, times(1)).isMoimActivityNotifyAllowed();
        verify(memberSetting, times(0)).isNoticeNotifyAllowed();

    }


    // isMemberFcmAllowed -> MOIM 알림일 경우, 금지된 경우
    @Test
    void isMemberNotifyAllowed_shouldReturnFalse_whenMoimActivityNotifyNotAllowed() {

        // given
        Member receiver = mock(Member.class);
        NotificationTopCategory topCategory = NotificationTopCategory.MOIM;
        MemberSetting memberSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
        when(memberSetting.isMoimActivityNotifyAllowed()).thenReturn(false);

        // when
        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);

        // then
        assertFalse(result);
        verify(memberSetting, times(1)).isMoimActivityNotifyAllowed();
        verify(memberSetting, times(0)).isNoticeNotifyAllowed();

    }


    // isMemberFcmAllowed -> NOTICE 알림일 경우, 허용된 경우
    @Test
    void isMemberNotifyAllowed_shouldReturnTrue_whenNoticeNotifyAllowed() {

        // given
        Member receiver = mock(Member.class);
        NotificationTopCategory topCategory = NotificationTopCategory.NOTICE;
        MemberSetting memberSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
        when(memberSetting.isNoticeNotifyAllowed()).thenReturn(true);

        // when
        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);

        // then
        assertTrue(result);
        verify(memberSetting, times(0)).isMoimActivityNotifyAllowed();
        verify(memberSetting, times(1)).isNoticeNotifyAllowed();

    }


    // isMemberFcmAllowed -> NOTICE 알림일 경우, 금지된 경우
    @Test
    void isMemberNotifyAllowed_shouldReturnFalse_whenNoticeNotifyNotAllowed() {

        // given
        Member receiver = mock(Member.class);
        NotificationTopCategory topCategory = NotificationTopCategory.NOTICE;
        MemberSetting memberSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
        when(memberSetting.isNoticeNotifyAllowed()).thenReturn(false);

        // when
        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);

        // then
        assertFalse(result);
        verify(memberSetting, times(0)).isMoimActivityNotifyAllowed();
        verify(memberSetting, times(1)).isNoticeNotifyAllowed();

    }


    // getAllowedReceiverFcmTokens (두개 만들어서 테스트)
    // Moim True, False 인 MemberSetting
    @Test
    void getAllowedReceiverFcmTokens_shouldCheckAllowedReceivers_whenMoimCategory() {

        // given
        Member memberA = mock(Member.class);
        Member memberB = mock(Member.class);
        MemberSetting memberASetting = mock(MemberSetting.class);
        MemberSetting memberBSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findByIds(any())).thenReturn(List.of(memberASetting, memberBSetting));
        when(memberASetting.isMoimActivityNotifyAllowed()).thenReturn(true);
        when(memberBSetting.isMoimActivityNotifyAllowed()).thenReturn(false);
        when(memberASetting.getMember()).thenReturn(memberA);

        // when
        memberSettingService.getAllowedReceiverFcmTokens(List.of(memberA, memberB), NotificationTopCategory.MOIM);

        // then
        verify(memberASetting, times(1)).isMoimActivityNotifyAllowed();
        verify(memberBSetting, times(1)).isMoimActivityNotifyAllowed();
        verify(memberA, times(1)).getFcmToken();

    }


    // Notice True, False 인 MemberSetting
    @Test
    void getAllowedReceiverFcmTokens_shouldCheckAllowedReceivers_whenNoticeCategory() {

        // given
        Member memberA = mock(Member.class);
        Member memberB = mock(Member.class);
        MemberSetting memberASetting = mock(MemberSetting.class);
        MemberSetting memberBSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findByIds(any())).thenReturn(List.of(memberASetting, memberBSetting));
        when(memberASetting.isNoticeNotifyAllowed()).thenReturn(false);
        when(memberBSetting.isNoticeNotifyAllowed()).thenReturn(true);
//        when(memberASetting.getMember()).thenReturn(memberA); 쓰이질 않으니 Unnecessary Mock 에러
        when(memberBSetting.getMember()).thenReturn(memberB);

        // when
        memberSettingService.getAllowedReceiverFcmTokens(List.of(memberA, memberB), NotificationTopCategory.NOTICE);

        // then
        verify(memberASetting, times(1)).isNoticeNotifyAllowed();
        verify(memberBSetting, times(1)).isNoticeNotifyAllowed();
//        verify(memberA, times(0)).getFcmToken();
        verify(memberB, times(1)).getFcmToken();

    }

    // Exceptional Case 없음
    // getMemberSetting // 정상
    @Test
    void getMemberSetting_shouldPass_whenRightInfoPassed() {
        // given
        Member member = mock(Member.class);
        MemberSetting ms = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(ms));

        // when
        memberSettingService.getMemberSetting(member);

        // then
        verify(memberSettingRepository, times(1)).findById(any());

    }


    // 못찾는 이상한 상황
    @Test
    void getMemberSetting_shouldThrowException_whenMemberSettingNotFound_byMoimingApiException() {
        // given
        Member member = mock(Member.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberSettingService.getMemberSetting(member)).isInstanceOf(MoimingApiException.class);
        verify(memberSettingRepository, times(1)).findById(any());

    }


    // updateNotifySetting
    // 정상
    @Test
    void updateNotifySetting_shouldCallChangeSetting_whenRightInfoPassed() {
        // given
        MemberSettingUpdateReqDto requestDto = mock(MemberSettingUpdateReqDto.class);
        Member member = mock(Member.class);
        MemberSetting memberSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));

        // when
        memberSettingService.updateNotifySetting(requestDto, member);

        // then
        verify(memberSettingRepository, times(1)).findById(any());
        verify(memberSetting, times(1)).changeNotifySettings(anyBoolean(), anyBoolean());

    }


    // 못찾는 이상한 상황
    @Test
    void updateNotifySetting_shouldThrowException_whenMemberSettingNotFound_byMoimingApiException() {
        // given
        MemberSettingUpdateReqDto requestDto = mock(MemberSettingUpdateReqDto.class);
        Member member = mock(Member.class);
        MemberSetting memberSetting = mock(MemberSetting.class);

        // given - stub
        when(memberSettingRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberSettingService.updateNotifySetting(requestDto, member)).isInstanceOf(MoimingApiException.class);
        verify(memberSettingRepository, times(1)).findById(any());
        verify(memberSetting, times(0)).changeNotifySettings(anyBoolean(), anyBoolean());

    }

}
