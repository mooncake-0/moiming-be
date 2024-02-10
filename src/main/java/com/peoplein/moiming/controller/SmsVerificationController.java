package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.auth.*;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.model.dto.response.AuthRespDto;
import com.peoplein.moiming.model.dto.response.MoimRespDto;
import com.peoplein.moiming.service.SmsVerificationService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.VerificationType.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.model.dto.response.AuthRespDto.*;

@Slf4j
@Api(tags = "SMS 문자인증 요청")
@RestController
@RequiredArgsConstructor
public class SmsVerificationController {

    private final SmsVerificationService smsVerificationService;

    /*
      AUTH 시 사용되는 휴대폰 SMS 인증시 사용
    */
    @ApiOperation("SMS 인증번호 송신 요청")
    @ApiResponses({
            @ApiResponse(code = 200, message = "SMS 인증 송신 성공", response = AuthSmsRespDto.class),
            @ApiResponse(code = 400, message = "SMS 인증 송신 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_AUTH_REQ_SMS_VERIFY)
    public ResponseEntity<?> processSmsVerification(@RequestBody @Valid AuthSmsReqDto requestDto
            , BindingResult br) {

        checkValidReqDto(requestDto);
        SmsVerification smsVerification = smsVerificationService.processSmsVerification(requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "SMS 문자 송신 성공", new AuthSmsRespDto(smsVerification)));

    }


    private void checkValidReqDto(AuthSmsReqDto requestDto) {

        if (requestDto == null || requestDto.getVerifyType() == null || (
                requestDto.getVerifyType().equals(FIND_ID) && !StringUtils.hasText(requestDto.getMemberName()) // FIND_ID 이지만 이름이 비어있을 경우
        ) || (
                requestDto.getVerifyType().equals(FIND_PW) && !StringUtils.hasText(requestDto.getMemberEmail()) // FIND_PW 이지만 이메일이 비어있음
        )) {
            log.error("{}, checkValidReqDto :: {}", this.getClass().getName(), "ReqDto 문제 or FIND_ID 인데 이름이 없거나, FIND_PW 인데 이메일이 없음");
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

    }

}