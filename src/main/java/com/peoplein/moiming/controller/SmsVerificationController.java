package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.auth.*;
import com.peoplein.moiming.service.SmsVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SMS 인증 처리 관련")
@RequiredArgsConstructor
@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_SMS_VER + NetworkSetting.API_SMS)
public class SmsVerificationController {

    private final SmsVerificationService smsVerificationService;

    /*
     ID 찾기 요청시 진행하는
     SMS 인증번호 요청
    */
    private ResponseModel<SmsVerificationDto> findMemberIdAuth(@RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseModel.createResponse(smsVerificationService.findMemberIdAuth(findIdRequestDto));
    }

    /*
     PW 찾기 요청시 진행하는
     SMS 인증번호 요청
    */
    private ResponseModel<SmsVerificationDto> findMemberPwAuth(@RequestBody FindPwRequestDto findPwRequestDto) {
        return ResponseModel.createResponse(smsVerificationService.findMemberPwAuth(findPwRequestDto));
    }

    /*
     PW 변경 요청시 진행하는
     SMS 인증번호 요청
    */
    private ResponseModel<SmsVerificationDto> changePwAuth(@RequestBody ChangePwRequestDto changePwRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(smsVerificationService.changePwAuth(curMember, changePwRequestDto));
    }


    /*
     SMS 인증 요청
     */
    private String verifyNumber(@RequestBody SmsVerifyRequestDto smsVerifyRequestDto) {
        return "";
    }
}
