package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.service.NotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_NOTI_VER + NetworkSetting.API_NOTI)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 조회와 삭제에 대한 요청 가능
    // 인증정보로 요청자 판단
    @GetMapping("")
    public String viewAllNotification() {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.viewAllNotification(curMember);
        return "";
    }

}
