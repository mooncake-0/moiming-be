package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.dto.auth.*;
import com.peoplein.moiming.service.SmsVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PostMapping("/send/findId")
    private ResponseEntity<SmsVerificationDto> findMemberIdAuth(@RequestBody FindIdRequestDto findIdRequestDto) {
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(smsVerificationService.findMemberIdAuth(findIdRequestDto));
    }

    /*
     PW 찾기 요청시 진행하는
     SMS 인증번호 요청
    */
    @PostMapping("/send/findPw")
    private ResponseEntity<SmsVerificationDto> findMemberPwAuth(@RequestBody FindPwRequestDto findPwRequestDto) {
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(smsVerificationService.findMemberPwAuth(findPwRequestDto));
    }


    /*
     PW 변경 요청시 진행하는
     SMS 인증번호 요청
    */
    @PostMapping("/send/changePw")
    private ResponseEntity<SmsVerificationDto> changePwAuth(@RequestBody ChangePwRequestDto changePwRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(smsVerificationService.changePwAuth(curMember, changePwRequestDto));
    }


    /*
     입력한 번호와
     서버단에 생성된 SmsVerification 객체의 Number 와 동일한지 확인한다
    */
    @PostMapping("/verify")
    private ResponseEntity<String> verifyNumber(@RequestBody SmsVerifyRequestDto smsVerifyRequestDto) {
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(smsVerificationService.verifyNumber(smsVerifyRequestDto));
    }

}