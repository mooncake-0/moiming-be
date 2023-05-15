package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.model.dto.auth.ChangePwRequestDto;
import com.peoplein.moiming.model.dto.auth.FindIdRequestDto;
import com.peoplein.moiming.model.dto.auth.FindPwRequestDto;
import com.peoplein.moiming.model.dto.auth.SmsVerificationDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Transactional
@RequiredArgsConstructor
public class SmsVerificationService {

    private final MemberRepository memberRepository;
    private final SmsVerificationRepository smsVerificationRepository;

    public SmsVerificationDto findMemberIdAuth(@RequestBody FindIdRequestDto findIdRequestDto) {
        Member curMember = memberRepository.findOptionalByPhoneNumber(findIdRequestDto.getMemberPhoneNumber()).orElseThrow(() -> new RuntimeException("해당 전화번호의 유저가 존재하지 않습니다"));
        checkRightMemberRequest(curMember, VerificationType.FIND_ID, findIdRequestDto.getMemberName());

        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getUid(), curMember.getMemberInfo().getMemberPhone(), VerificationType.FIND_ID);

        // TODO 문자 진행

        smsVerificationRepository.save(smsVerification);

        return new SmsVerificationDto(smsVerification.getId());
    }

    public SmsVerificationDto findMemberPwAuth(@RequestBody FindPwRequestDto findPwRequestDto) {
        Member curMember = memberRepository.findOptionalByPhoneNumber(findPwRequestDto.getMemberPhoneNumber()).orElseThrow(() -> new RuntimeException("해당 전화번호의 유저가 존재하지 않습니다"));
        checkRightMemberRequest(curMember, VerificationType.FIND_PW, findPwRequestDto.getMemberEmail());

        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getUid(), curMember.getMemberInfo().getMemberPhone(), VerificationType.FIND_PW);

        // TODO 문자 진행

        smsVerificationRepository.save(smsVerification);

        return new SmsVerificationDto(smsVerification.getId());
    }

    public SmsVerificationDto changePwAuth(Member curMember, ChangePwRequestDto changePwRequestDto) {
        checkRightMemberRequest(curMember, VerificationType.PW_CHANGE, changePwRequestDto.getMemberPhoneNumber());

        SmsVerification smsVerification = SmsVerification.createSmsVerification(curMember.getUid(), curMember.getMemberInfo().getMemberPhone(), VerificationType.PW_CHANGE);

        // TODO 문자 진행

        smsVerificationRepository.save(smsVerification);

        return new SmsVerificationDto(smsVerification.getId());
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
            if (!curMember.getMemberInfo().getMemberEmail().equals(info)) {
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


    // 공통적으로 하는 일
    // 1. Member 를 찾는다
    //     1-1 ID 찾기 --> 번호로 Member 조회, 이름 매칭하는지 확인
    //     1-2 PW 찾기 --> 번호로 Member 조회, email 매칭하는지 확인
    //     1-3 PW 변경 --> 이건 Member Authentication 객체가 있음 > 번호 확인 필요  // 해당 Member 의 번호가 맞는지 확인
    // 2. SmsVerification 을 생성 후 SMS 문자를 보내준다
    // 3. 확인 요청시 확인됨을 보낸다
}
