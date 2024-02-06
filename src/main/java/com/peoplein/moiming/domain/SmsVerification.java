package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Random;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

/*
    TODO :: 의문 > 바로 삭제되는 것이 관리 차원에서 맞을까? (Id 찾은, Pw 찾은 이력이 남지 않는다)
            바로 삭제가 되지 않는다면, 후속 요청시 직전에 판단한 SMS VERIFICATION 이 Verified 되었다는
            것을 확인할 수 있어야함
            (예를들어, Id 찾기 요청 >> 인증번호 SMS 생성
                    SMS 인증 확인 요청 >> 인증됨 (is_verified = True)
                    그 후 ID 찾기 요청 >> 수신시 해당 SMS VERIFICATION 을 찾아서 is_verified = True 임을 확인해야함
             이 때, 그 SMS_VERIFICATION 객체를 어떻게 찾을 것인가?)
 */
/*
    TODO :: 일단 SMS Verification 의 PK 값을 주고 받는것으로 판단한다
            그리고 모든 인증 시도는 저장한다

 */
@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsVerification {

    private static final int VERIFICATION_NUM_LENGTH = 6;
    private static final String VERIFICATION_NUM_CHARS = "0123456789";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_verification_id")
    private Long id;

    private String verificationNumber;

    private Long memberId;

    private String memberPhoneNumber;

    private boolean isVerified;

    @Enumerated(value = EnumType.STRING)
    private VerificationType verificationType; // 요청의 의도를 파악하기 위해 필요 (해당 Member 의 SMS VERIFICATION 이 있는지 확인시)

    // 따로 관리하기 위해 BaseEntity 사용하지 않음. Update 도 필요 없음
    private LocalDateTime createdAt;

    private LocalDateTime expiredAt; // 만료 및 추후 일괄 삭제시 필요

    public static SmsVerification createSmsVerification(Long memberId, String memberPhoneNumber, VerificationType verificationType) {
        String verificationNumber = createVerificationNumber();
        return new SmsVerification(verificationNumber, memberId, memberPhoneNumber, verificationType);
    }

    private SmsVerification(String verificationNumber, Long memberId, String memberPhoneNumber, VerificationType verificationType) {

        this.verificationNumber = verificationNumber;
        this.memberId = memberId;
        this.memberPhoneNumber = memberPhoneNumber;
        this.verificationType = verificationType;

        // 초기화
        this.isVerified = false;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = this.createdAt.plusMinutes(3); // 만료 시간은 생성 이후로 3분
    }


    /*
     생성시 Random 한 VerificationNumber 을 생성해준다
     */
    private static String createVerificationNumber() {

        Random random = new Random();
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < VERIFICATION_NUM_LENGTH; i++) {
            int randomIndex = random.nextInt(VERIFICATION_NUM_CHARS.length());
            char randomChar = VERIFICATION_NUM_CHARS.charAt(randomIndex);
            strBuilder.append(randomChar);
        }

        return strBuilder.toString();
    }


    public void confirmVerification(VerificationType type, String verificationNumber) {

        if (!this.verificationNumber.equals(verificationNumber)) {
            log.error("{} confirmVerification :: {}", this.getClass().getName(), AUTH_SMS_VERIFICATION_NUMBER_NOT_MATCH.getErrMsg());
            throw new MoimingAuthApiException(AUTH_SMS_VERIFICATION_NUMBER_NOT_MATCH);
        }

        checkVerificationType(type);
        checkExpiration();

        this.isVerified = true;
    }


    public void isValidAndVerified(VerificationType type) {

        if (!this.isVerified) {
            log.error("{} isValidAndVerified :: {}", this.getClass().getName(), AUTH_SMS_NOT_VERIFIED.getErrMsg());
            throw new MoimingAuthApiException(AUTH_SMS_NOT_VERIFIED);
        }

        checkVerificationType(type);
        checkExpiration();
    }


    private void checkVerificationType(VerificationType type) {

        if (!this.verificationType.equals(type)) {
            log.error("{} checkVerificationType :: {}", this.getClass().getName(), AUTH_SMS_VERIFICATION_TYPE_NOT_MATCH.getErrMsg());
            throw new MoimingAuthApiException(AUTH_SMS_VERIFICATION_TYPE_NOT_MATCH);
        }

    }


    private void checkExpiration() {

        if (this.expiredAt.isBefore(LocalDateTime.now())) {
            log.error("{} checkExpiration :: {}", this.getClass().getName(), AUTH_SMS_VERIFICATION_EXPIRED.getErrMsg());
            throw new MoimingAuthApiException(AUTH_SMS_VERIFICATION_EXPIRED);
        }
    }
}
