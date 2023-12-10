package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.model.dto.auth.*;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import com.peoplein.moiming.service.core.SmsVerificationCore;
import com.peoplein.moiming.service.shell.SmsSendShell;
import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Transactional
@RequiredArgsConstructor
public class SmsVerificationService {

    private final MemberRepository memberRepository;
    private final SmsVerificationRepository smsVerificationRepository;
    private final SmsVerificationCore smsVerificationCore;
    private final SmsSendShell smsSendShell;

    public SmsVerificationDto findMemberIdAuth(@RequestBody FindIdRequestDto findIdRequestDto) {

        Member curMember = memberRepository.findByPhoneNumber(findIdRequestDto.getMemberPhoneNumber()).orElseThrow(() -> new RuntimeException("해당 전화번호의 유저가 존재하지 않습니다"));
        checkRightMemberRequest(curMember, VerificationType.FIND_ID, findIdRequestDto.getMemberName());

        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getId(), curMember.getMemberInfo().getMemberPhone(), VerificationType.FIND_ID);

        // 문자 진행
        buildAndSendMessage(smsVerification.getVerificationNumber(), smsVerification.getMemberPhoneNumber());

        smsVerificationRepository.save(smsVerification);

        return new SmsVerificationDto(smsVerification.getId());
    }

    public SmsVerificationDto findMemberPwAuth(@RequestBody FindPwRequestDto findPwRequestDto) {

        Member curMember = memberRepository.findByPhoneNumber(findPwRequestDto.getMemberPhoneNumber()).orElseThrow(() -> new RuntimeException("해당 전화번호의 유저가 존재하지 않습니다"));
        checkRightMemberRequest(curMember, VerificationType.FIND_PW, findPwRequestDto.getMemberEmail());

        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getId(), curMember.getMemberInfo().getMemberPhone(), VerificationType.FIND_PW);

        // 문자 진행
        buildAndSendMessage(smsVerification.getVerificationNumber(), smsVerification.getMemberPhoneNumber());

        smsVerificationRepository.save(smsVerification);

        return new SmsVerificationDto(smsVerification.getId());
    }

    public SmsVerificationDto changePwAuth(Member curMember, ChangePwRequestDto changePwRequestDto) {

        checkRightMemberRequest(curMember, VerificationType.PW_CHANGE, changePwRequestDto.getMemberPhoneNumber());

        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getId(), curMember.getMemberInfo().getMemberPhone(), VerificationType.PW_CHANGE);

        // 문자 진행
        buildAndSendMessage(smsVerification.getVerificationNumber(), smsVerification.getMemberPhoneNumber());

        smsVerificationRepository.save(smsVerification);

        return new SmsVerificationDto(smsVerification.getId());
    }

    /*
     문자 보내는 함수
     */
    private void buildAndSendMessage(String verificationNumber, String memberPhoneNumber) {
        Request request = smsVerificationCore.buildResponse(verificationNumber, memberPhoneNumber);
        smsSendShell.sendMessage(request);
    }


    /*
     핸드폰번호로 Member 를 조회하고, 상황에 맞게 부가 정보로 일치성 여부를 확인한다
     */
    private void checkRightMemberRequest(Member curMember, VerificationType verificationType, String info) {

        if (verificationType.equals(VerificationType.FIND_ID)) { // info = 이름
            if (!curMember.getMemberInfo().getMemberName().equals(info)) {
                throw new RuntimeException("ID 찾기 오류 :: 해당 번호 유저의 이름이 아닙니다");
            }
        }

        if (verificationType.equals(VerificationType.FIND_PW)) { // info = email
            if (!curMember.getMemberEmail().equals(info)) {
                throw new RuntimeException("PW 찾기 오류 :: 해당 번호 유저의 이메일이 아닙니다");
            }
        }

        if (verificationType.equals(VerificationType.PW_CHANGE)) { // info = phone_number
            if (!curMember.getMemberInfo().getMemberPhone().equals(info)) {
                throw new RuntimeException("PW 변경 오류 :: 요청한 유저의 전화번호가 아닙니다");
            }
        }

        // ...

    }

    public String verifyNumber(SmsVerifyRequestDto smsVerifyRequestDto) {

        SmsVerification smsVerification = smsVerificationRepository.findOptionalById(smsVerifyRequestDto.getSmsVerificationId()).orElseThrow(() -> new RuntimeException("존재하지 않는 인증 시도입니다"));

        if (!smsVerification.getVerificationNumber().equals(smsVerifyRequestDto.getInputVerificationNumber())) {
            throw new RuntimeException("인증번호가 일치하지 않습니다");
        }

        smsVerification.setVerified(true);

        return "OK";
    }

}
