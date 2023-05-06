package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notification 관련")
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_NOTI_VER + NetworkSetting.API_NOTI)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 조회와 삭제에 대한 요청 가능
    // 인증정보로 요청자 판단
    @Operation(summary = "멤버 모든 알림 조회", description = "멤버의 모든 알림 정보를 반환한다")
    @GetMapping("")
    public String viewAllNotification() {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.viewAllNotification(curMember);
        return "";
    }

}
