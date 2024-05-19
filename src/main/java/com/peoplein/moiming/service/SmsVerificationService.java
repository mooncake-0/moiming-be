package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import com.peoplein.moiming.service.external.ExternalReqSender;
import com.peoplein.moiming.service.util.sms.SmsRequestBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.peoplein.moiming.domain.enums.VerificationType.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;


// TODO :: 고의적 연속 요청에 대한 방어로직은 어떻게 할 수 없을까?

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SmsVerificationService {

    private final MemberRepository memberRepository;
    private final SmsVerificationRepository smsVerificationRepository;
    private final SmsRequestBuilder smsRequestBuilder;
    private final ExternalReqSender externalReqSender;


    /*
     v0.1 - ID (당시 Email) 찾기와 PW 재설정에 대한 인증 문자를
            외부 서버로 요청하는 요청
     v0.2 - 위외 같이 계속 사용되나, 더이상 인앱에서 ID 찾기란 없고, memberName 이란 값도 없다
     */
    public SmsVerification processSmsVerification(AuthSmsReqDto requestDto) {

        if (requestDto == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        SmsVerification smsVerification = null;

        if (requestDto.getVerifyType().equals(SIGN_UP)) {
            smsVerification = SmsVerification.createSmsVerification(null, requestDto.getMemberPhone(), SIGN_UP);
        } else if (requestDto.getVerifyType().equals(FIND_PW)) {
            // 핸드폰 번호로 유저를 찾고, 받아온 값들로 verification 을 우선 진행한다
            Member curMember = memberRepository.findByMemberPhone(requestDto.getMemberPhone()).orElseThrow(() -> {
                        log.error("{}, processSmsVerification :: {}", this.getClass().getName(), "[" + requestDto.getMemberPhone() + "] 유저의 SMS 시도, 존재하지 않는 회원 예외 발생");
                        return new MoimingApiException(MEMBER_NOT_FOUND);
                    }
            );
            smsVerification = SmsVerification.createSmsVerification(curMember.getId(), curMember.getMemberPhone(), FIND_PW);

        } else { // 에러 방지
            log.error("{}, processSmsVerification :: {}", this.getClass().getName(), "[" + requestDto.getVerifyType() + "] unexpected verification type");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        Request request = smsRequestBuilder.getHttpRequest(smsVerification);
        externalReqSender.sendAsynchronousMessage(request);

        smsVerificationRepository.save(smsVerification);

        return smsVerification;

    }


    // SMS 인증을 인증 번호를 통해 진행한다
    public SmsVerification getVerifiedSmsVerification(Long smsVerificationId, VerificationType type, String verificationNumber) {

        if (smsVerificationId == null || type == null || !StringUtils.hasText(verificationNumber)) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        SmsVerification smsVerification = smsVerificationRepository.findById(smsVerificationId).orElseThrow(() -> {
            log.error("{} getVerifiedSmsVerification :: {}", this.getClass().getName(), AUTH_SMS_VERIFICATION_NOT_FOUND.getErrMsg());
            return new MoimingAuthApiException(AUTH_SMS_VERIFICATION_NOT_FOUND);
        });

        smsVerification.confirmVerification(type, verificationNumber);

        return smsVerification;
    }


    // SMS 인증 정보가 유효한지 판단 후 반환한다
    // SMS 인증 정보를 사용하기 위함 (현재 MVP 에선 [비밀번호 재설정] 만 사용)
    public SmsVerification confirmAndGetValidSmsVerification(VerificationType type, Long smsVerificationId) {

        if (smsVerificationId == null || type == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        SmsVerification smsVerification = smsVerificationRepository.findById(smsVerificationId).orElseThrow(() -> {
            log.error("{} getVerifiedSmsVerification :: {}", this.getClass().getName(), AUTH_SMS_VERIFICATION_NOT_FOUND.getErrMsg());
            return new MoimingAuthApiException(AUTH_SMS_VERIFICATION_NOT_FOUND);
        });

        smsVerification.isValidAndVerified(type);

        return smsVerification;
    }

}
