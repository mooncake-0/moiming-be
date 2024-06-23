package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.member.MemberSetting;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.MemberInfoReqDto;
import com.peoplein.moiming.model.dto.request.MemberSettingReqDto;
import com.peoplein.moiming.model.dto.response.MemberSettingRespDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.MemberSettingService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


import javax.validation.Valid;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MemberSettingReqDto.*;
import static com.peoplein.moiming.model.dto.response.MemberSettingRespDto.*;

@Api(tags = "회원 설정 관련 (현재 알림 허용 여부만)")
@RestController
@RequiredArgsConstructor
public class MemberSettingController {

    private final MemberSettingService memberSettingService;


    @ApiOperation("유저 알림 설정 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저 알림 설정 조회 성공", response = MemberSettingViewRespDto.class),
            @ApiResponse(code = 400, message = "유저 알림 설정 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_GET_MEMBER_NOTIFY_SETTING)
    public ResponseEntity<?> getMemberNotifySetting(@AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        MemberSetting memberSetting = memberSettingService.getMemberSetting(principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "조회 성공", new MemberSettingViewRespDto(memberSetting)));

    }


    @ApiOperation("유저 알림 설정 수정 - 매 요청시 두 notify 현재 상태 모두 전달 (요청에 항상 안바뀌는 것도 포함되어 있음)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저 알림 설정 수정 성공", response = MemberSettingNotifyUpdateRespDto.class),
            @ApiResponse(code = 400, message = "유저 알림 설정 수정, ERR MSG 확인")
    })
    @PatchMapping(PATH_UPDATE_MEMBER_NOTIFY_SETTING)
    public ResponseEntity<?> updateMemberNotifySetting(
            @RequestBody @Valid MemberSettingUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        MemberSetting memberSetting = memberSettingService.updateNotifySetting(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "수정 성공", new MemberSettingNotifyUpdateRespDto(memberSetting)));
    }
}
