package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.enums.NotificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;


    @Test
    void createNotification_shouldPass_whenRightInfoPassed() {

        // given
        NotificationTopCategory topCategory = mock(NotificationTopCategory.class);
        NotificationSubCategory subCategory = mock(NotificationSubCategory.class);
        NotificationType type = mock(NotificationType.class);
        Long receiverId = 1L; // NOT_NULL

        // when
        notificationService.createNotification(topCategory, subCategory, type, receiverId, "", "", null, null);

        // then
        verify(notificationRepository, times(1)).save(any());

    }


    @Test
    void getMemberNotification_shouldPass_whenRightInfoWithoutLastNotificationIdPassed() {

        // given
        Member member = mock(Member.class);
        NotificationTopCategory topCategory = mock(NotificationTopCategory.class);

        // when
        notificationService.getMemberNotification(member, topCategory, "", null, 0);

        // then
        verify(notificationRepository, times(1)).findMemberNotificationByCondition(any(), any(), any(), any(), anyInt());

    }


    @Test
    void getMemberNotification_shouldPass_whenRightInfoWithLastNotificationIdPassed() {

        // given
        Member member = mock(Member.class);
        NotificationTopCategory topCategory = mock(NotificationTopCategory.class);
        Notification lastNotification = mock(Notification.class);

        // given - stub
        when(notificationRepository.findById(any())).thenReturn(Optional.of(lastNotification));

        // when
        notificationService.getMemberNotification(member, topCategory, "", 1L, 0);

        // then
        verify(notificationRepository, times(1)).findMemberNotificationByCondition(any(), any(), any(), any(), anyInt());

    }


    @Test
    void getMemberNotification_shouldThrowException_whenLastNotificationNotFound_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        NotificationTopCategory topCategory = mock(NotificationTopCategory.class);

        // given - stub
        when(notificationRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> notificationService.getMemberNotification(member, topCategory, "", 1L, 0)).isInstanceOf(MoimingApiException.class);
        verify(notificationRepository, times(0)).findMemberNotificationByCondition(any(), any(), any(), any(), anyInt());

    }


    @Test
    void deleteNotification_shouldPass_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        Long notificationId = 1L;
        Notification notification = mock(Notification.class);

        // given - stub
        when(notificationRepository.findById(any())).thenReturn(Optional.of(notification));
        when(member.getId()).thenReturn(0L);
        when(notification.getReceiverId()).thenReturn(0L); // Notification Receiver 명시

        // when
        notificationService.deleteNotification(member, notificationId);

        // then
        verify(notificationRepository, times(1)).remove(any());

    }


    @Test
    void deleteNotification_shouldThrowException_whenNotificationNotFound_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        Long notificationId = 1L;

        // given - stub
        when(notificationRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> notificationService.deleteNotification(member, notificationId)).isInstanceOf(MoimingApiException.class);
        verify(notificationRepository, times(0)).remove(any());

    }


    @Test
    void deleteNotification_shouldThrowException_whenMemberNotAuthorized_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        Long notificationId = 1L;
        Notification notification = mock(Notification.class);

        // given - stub
        when(notificationRepository.findById(any())).thenReturn(Optional.of(notification));
        when(member.getId()).thenReturn(0L);
        when(notification.getReceiverId()).thenReturn(100L); // Member Not Receiver

        // when
        // then
        assertThatThrownBy(() -> notificationService.deleteNotification(member, notificationId)).isInstanceOf(MoimingApiException.class);
        verify(notificationRepository, times(0)).remove(any());

    }
}
