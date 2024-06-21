//package com.peoplein.moiming.service;
//
//import com.peoplein.moiming.domain.enums.NotificationTopCategory;
//import com.peoplein.moiming.domain.member.Member;
//import com.peoplein.moiming.domain.member.MemberSetting;
//import com.peoplein.moiming.repository.jpa.MemberSettingRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class MemberSettingServiceTest {
//
//    @InjectMocks
//    private MemberSettingService memberSettingService;
//
//    @Mock
//    private MemberSettingRepository memberSettingRepository;
//
//
//    // NOTICE 통과 / 실패
//    // Member Setting 못찾음 -> 굳이 예외까진 던지지 않음
//    @Test
//    void isMemberNotifyAllowed_shouldReturnFalse_whenMemberSettingNotFound() {
//
//        // given
//        Member receiver = mock(Member.class);
//        NotificationTopCategory topCategory = mock(NotificationTopCategory.class);
//
//        // given - stub
//        when(memberSettingRepository.findById(any())).thenReturn(Optional.empty());
//
//        // when
//        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);
//
//        // then
//        assertFalse(result);
//
//    }
//
//
//    // isMemberFcmAllowed -> MOIM 알림일 경우, 허요된 경우
//    @Test
//    void isMemberNotifyAllowed_shouldReturnTrue_whenMoimActivityNotifyAllowed() {
//
//        // given
//        Member receiver = mock(Member.class);
//        NotificationTopCategory topCategory = NotificationTopCategory.MOIM;
//        MemberSetting memberSetting = mock(MemberSetting.class);
//
//        // given - stub
//        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
//        when(memberSetting.isMoimActivityNotifyAllowed()).thenReturn(true);
//
//        // when
//        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);
//
//        // then
//        assertTrue(result);
//        verify(memberSetting, times(1)).isMoimActivityNotifyAllowed();
//        verify(memberSetting, times(0)).isNoticeNotifyAllowed();
//
//    }
//
//
//    // isMemberFcmAllowed -> MOIM 알림일 경우, 금지된 경우
//    @Test
//    void isMemberNotifyAllowed_shouldReturnFalse_whenMoimActivityNotifyNotAllowed() {
//
//        // given
//        Member receiver = mock(Member.class);
//        NotificationTopCategory topCategory = NotificationTopCategory.MOIM;
//        MemberSetting memberSetting = mock(MemberSetting.class);
//
//        // given - stub
//        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
//        when(memberSetting.isMoimActivityNotifyAllowed()).thenReturn(false);
//
//        // when
//        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);
//
//        // then
//        assertFalse(result);
//        verify(memberSetting, times(1)).isMoimActivityNotifyAllowed();
//        verify(memberSetting, times(0)).isNoticeNotifyAllowed();
//
//    }
//
//
//    // isMemberFcmAllowed -> NOTICE 알림일 경우, 허용된 경우
//    @Test
//    void isMemberNotifyAllowed_shouldReturnTrue_whenNoticeNotifyAllowed() {
//
//        // given
//        Member receiver = mock(Member.class);
//        NotificationTopCategory topCategory = NotificationTopCategory.NOTICE;
//        MemberSetting memberSetting = mock(MemberSetting.class);
//
//        // given - stub
//        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
//        when(memberSetting.isMoimActivityNotifyAllowed()).thenReturn(true);
//
//        // when
//        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);
//
//        // then
//        assertTrue(result);
//        verify(memberSetting, times(0)).isMoimActivityNotifyAllowed();
//        verify(memberSetting, times(1)).isNoticeNotifyAllowed();
//
//    }
//
//
//    // isMemberFcmAllowed -> NOTICE 알림일 경우, 금지된 경우
//    @Test
//    void isMemberNotifyAllowed_shouldReturnFalse_whenNoticeNotifyNotAllowed() {
//
//        // given
//        Member receiver = mock(Member.class);
//        NotificationTopCategory topCategory = NotificationTopCategory.NOTICE;
//        MemberSetting memberSetting = mock(MemberSetting.class);
//
//        // given - stub
//        when(memberSettingRepository.findById(any())).thenReturn(Optional.of(memberSetting));
//        when(memberSetting.isMoimActivityNotifyAllowed()).thenReturn(false);
//
//        // when
//        boolean result = memberSettingService.isMemberNotifyAllowed(receiver, topCategory);
//
//        // then
//        assertFalse(result);
//        verify(memberSetting, times(0)).isMoimActivityNotifyAllowed();
//        verify(memberSetting, times(1)).isNoticeNotifyAllowed();
//
//    }
//}
