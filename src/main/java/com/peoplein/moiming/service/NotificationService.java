package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.enums.NotificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.peoplein.moiming.exception.ExceptionValue.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(NotificationTopCategory topCategory, NotificationSubCategory subCategory, NotificationType type
            , Long receiverId, String title, String body, Long topCategoryId, Long subCategoryId) {

        if (topCategory == null || subCategory == null || type== null || receiverId == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Notification notification = Notification.createNotification(topCategory, subCategory, type, receiverId, title, body, topCategoryId, subCategoryId);
        notificationRepository.save(notification);

        // TODO :: FCM 관련 처리
    }


    // TODO :: FCM 다량 요청 확인 후 점검
    @Transactional
    public void createManyNotification() {

    }


    @Transactional(readOnly = true)
    public List<Notification> getMemberNotification(Member member, NotificationTopCategory topCategory, String moimType, Long lastNotificationId, int limit) {

        if (member == null || topCategory == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }


        Notification lastNotification = null;
        if (lastNotificationId != null) {
            lastNotification = notificationRepository.findById(lastNotificationId).orElseThrow(() -> {
                        log.info("{}, getMemberNotification :: {}", this.getClass().getName(), "[" + member.getId() + "]의 알림 조회 중 존재하지 않는 이전 알림 조회");
                        return new MoimingApiException(MEMBER_NOTIFICATION_NOT_FOUND);
                    }
            );
        }

        return notificationRepository.findMemberNotificationByCondition(member.getId(), topCategory, moimType, lastNotification, limit);

        // TODO :: 모임의 이미지들이 필요하다. Notification 의 subCategory Id 로 Moim Img 를 가져오든, 아니면 위 쿼리에서 병합해서 가져오든 해야 할 듯 하다
        //         쿼리 병합은 안좋은 생각인듯. 응답에 어떤 Value 들이 추가될 지 모르니, 그냥 Moim 을 모두 조회 후 외부로 전달하는게 적합해 보인다
    }


    @Transactional
    public void deleteNotification(Member member, Long notificationId) {

        if (member == null || notificationId == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // notificationId 조회 우선
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> {
                    log.info("{}, deleteNotification :: {}", this.getClass().getName(), "[" + member.getId() + "]의 알림 삭제 중 존재하지 않는 알림 조회");
                    return new MoimingApiException(MEMBER_NOTIFICATION_NOT_FOUND);
                }
        );

        // 권한 확인
        if (!notification.getReceiverId().equals(member.getId())) {
            log.error("{}, deleteNotification :: {}", this.getClass().getName(), "[" + member.getId() + "]의 본인 소유가 아닌 알림 삭제 시도");
            throw new MoimingApiException(MEMBER_NOT_AUTHORIZED);
        }

        notificationRepository.remove(notification);
    }

}
