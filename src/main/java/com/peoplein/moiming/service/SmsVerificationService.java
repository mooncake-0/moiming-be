package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import com.peoplein.moiming.service.util.sms.NaverSmsRequestBuilder;
import com.peoplein.moiming.service.external.SmsSender;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SmsVerificationService {

    private final MemberRepository memberRepository;
    private final SmsVerificationRepository smsVerificationRepository;
    private final SmsRequestBuilder smsRequestBuilder;
    private final SmsSender smsSender;


    public SmsVerification processSmsVerification(AuthSmsReqDto requestDto) {

        if (requestDto == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 핸드폰 번호로 유저를 찾고, 받아온 값들로 verification 을 우선 진행한다
        Member curMember = memberRepository.findWithMemberInfoByPhoneNumber(requestDto.getMemberPhone()).orElseThrow(() -> {
                    log.error("{}, {} :: 유저의 SMS 시도, 존재하지 않는 회원 예외 발생", requestDto.getMemberPhone(), requestDto.getMemberName());
                    return new MoimingApiException(MEMBER_NOT_FOUND);
                }
        );

        checkRightMemberRequest(curMember, requestDto);
        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getId(), curMember.getMemberInfo().getMemberPhone(), requestDto.getVerifyType());

        Request request = smsRequestBuilder.getHttpRequest(smsVerification);
        smsSender.sendMessage(request);

        smsVerificationRepository.save(smsVerification);

        return smsVerification;

    }


    /*
     핸드폰번호로 Member 를 조회하고, 상황에 맞게 부가 정보로 일치성 여부를 확인한다
     */
    private void checkRightMemberRequest(Member curMember, AuthSmsReqDto requestDto) {

        if (requestDto.getVerifyType().equals(FIND_ID)) {
            if (!curMember.getMemberInfo().getMemberName().equals(requestDto.getMemberName())) {
                log.error("{}, 전달이름 {} :: 유저의 SMS ID 찾기 시도, 조회결과 전달받은 이름 불일치", requestDto.getMemberPhone(), requestDto.getMemberName());
                throw new MoimingAuthApiException(AUTH_SMS_INVALID_NAME_WITH_PHONE);
            }
        }

        if (requestDto.getVerifyType().equals(FIND_PW)) {
            if (!curMember.getMemberEmail().equals(requestDto.getMemberEmail())) {
                log.error("{}, 전달이메일 {} :: 유저의 SMS 비밀번호 변경 시도, 조회결과 전달받은 이메일 불일치", requestDto.getMemberPhone(), requestDto.getMemberEmail());
                throw new MoimingAuthApiException(AUTH_SMS_INVALID_NAME_WITH_EMAIL);
            }
        }

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
