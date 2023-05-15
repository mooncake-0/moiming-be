package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.VerificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Random;

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

 */
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

    private String memberUid;

    private String memberPhoneNumber;


    private boolean isVerified;

    @Enumerated(value = EnumType.STRING)
    private VerificationType verificationType; // 요청의 의도를 파악하기 위해 필요 (해당 Member 의 SMS VERIFICATION 이 있는지 확인시)

    // 따로 관리하기 위해 BaseEntity 사용하지 않음. Update 도 필요 없음
    private LocalDateTime createdAt;

    private LocalDateTime expiredAt; // 만료 및 추후 일괄 삭제시 필요

    public static SmsVerification createSmsVerification(String memberUid, String memberPhoneNumber, VerificationType verificationType) {
        String verificationNumber = createVerificationNumber();
        return new SmsVerification(verificationNumber, memberUid, memberPhoneNumber, verificationType);
    }

    private SmsVerification(String verificationNumber, String memberUid, String memberPhoneNumber, VerificationType verificationType) {

        DomainChecker.checkRightString(getClass().getName(), false, verificationNumber, memberUid, memberPhoneNumber);
        DomainChecker.checkWrongObjectParams(getClass().getName(), verificationType);

        this.verificationNumber = verificationNumber;
        this.memberUid = memberUid;
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


}
