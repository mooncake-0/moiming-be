package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationDomain;
import com.peoplein.moiming.domain.enums.NotificationDomainCategory;
import com.peoplein.moiming.model.dto.domain.NotificationDto;
import com.peoplein.moiming.repository.NotificationRepository;
import com.peoplein.moiming.service.shell.NotificationServiceShell;
import com.peoplein.moiming.service.support.FcmService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final NotificationServiceShell notificationServiceShell;

    public NotificationService(FcmService fcmService, NotificationRepository notificationRepository, NotificationServiceShell notificationServiceShell) {
        this.fcmService = fcmService;
        this.notificationRepository = notificationRepository;
        this.notificationServiceShell = notificationServiceShell;
    }

    public void viewAllNotification(Member curMember) {


    }

    public void createNotification(NotificationDto notificationDto, Member receiver) {

        // 1. 수신된 notificationDto 를 가지고 어떤 noti 인지 파악한다
        List<String> info = buildTitleAndBody(notificationDto);

        // 2. Notification Entity 를 만들어 저장한다
        Notification notification = Notification.createNotification(notificationDto.getSenderId()
                , info.get(0), info.get(1)
                , notificationDto.getDomainId(), notificationDto.getNotiDomain(), notificationDto.getNotiCategory()
                , receiver);

        notificationRepository.save(notification);

        // 3. FCM 을 생성하여 전송한다
        fcmService.sendSingleMessageTo(notification.getMember().getFcmToken()
                , notification.getNotiTitle()
                , notification.getNotiBody());

        // 종료
    }


    // Noti 에 대한 정보를 토대로 제목과 내용을 작성한다
    private List<String> buildTitleAndBody(NotificationDto notificationDto) {

        List<String> res = new ArrayList<>();
        String title = "";
        String body = "";

        // NotificationDomain 과 Category 에 다른 종류별 MSG BUILD
        if (notificationDto.getNotiDomain().equals(NotificationDomain.MOIM)) {

            notificationServiceShell.initMoim(notificationDto.getDomainId());
            Moim moim = notificationServiceShell.getMoim();

            if (notificationDto.getNotiCategory().equals(NotificationDomainCategory.MOIM_NEW_MEMBER)) {
                title = "모임 가입 수락 알림";
                body = moim.getMoimName() + " 모임에서 가입을 수락하였습니다. 지금 바로 모임활동에 참여해보세요";
            }

            if (notificationDto.getNotiCategory().equals(NotificationDomainCategory.MOIM_DECLINE_MEMBER)) {
                title = "모임 가입 거절 알림";
                body = moim.getMoimName() + " 모임에서 가입을 거절하였습니다. 거절 사유를 확인해보세요";
            }

        }

        res.add(title);
        res.add(body);

        return res;
    }
}
