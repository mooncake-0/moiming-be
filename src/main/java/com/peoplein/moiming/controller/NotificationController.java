package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.NotificationService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.response.NotificationRespDto.*;

@Api(tags = "알림 관련")
@RestController
@RequiredArgsConstructor
public class NotificationController {


    private final NotificationService notificationService;


    @ApiOperation("알림 조회 - 알림 종류 필수 지정 (현재는 [모임]만 필요), moimType = manage, join 가능 (알림 종류 [모임] 일 때만 지정 가능), 최신 작성일 순서대로 정렬, lastId 필요, 20개씩 전달")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "알림 일반 조회 성공", response = MemberNotificationRespDto.class),
            @ApiResponse(code = 400, message = "알림 일반 조회 실패")
    })
    @GetMapping(PATH_MEMBER_NOTIFICATIONS)
    public ResponseEntity<?> getMemberNotification(
            @RequestParam(required = false, value = "topCategory") String topCategory
            , @RequestParam(required = false, value = "moimType", defaultValue = "") String moimType
            , @RequestParam(required = false, value = "lastNotificationId") Long lastNotificationId
            , @RequestParam(required = false, defaultValue = "20") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        if (!StringUtils.hasText(topCategory) || // 알림 종류가 비어 있거나
                (StringUtils.hasText(moimType) && !moimType.equals("manage") && !moimType.equals("join"))) { // 모임 Type 이 지정되어 있는데, manage, join 이 아니다
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_REQUEST_PARAM);
        }

        NotificationTopCategory category = NotificationTopCategory.fromQueryParam(topCategory);
        List<Notification> memberNotifications = notificationService.getMemberNotification(principal.getMember(), category, moimType, lastNotificationId, limit);
        List<MemberNotificationRespDto> responseBody = memberNotifications.stream().map(MemberNotificationRespDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "알림 일반 조회 성공", responseBody));
    }


    // 알림 삭제
    @ApiOperation("알림 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "알림 삭제 성공"),
            @ApiResponse(code = 400, message = "알림 삭제 실패")
    })
    @DeleteMapping(PATH_MEMBER_NOTIFICATION_DELETE)
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        notificationService.deleteNotification(principal.getMember(), notificationId);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "알림 삭제 성공", null));
    }


}
