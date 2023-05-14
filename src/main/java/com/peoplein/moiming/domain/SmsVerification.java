package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.VerificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsVerification {

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

    public SmsVerification createSmsVerification(String verificationNumber, String memberUid, String memberPhoneNumber, VerificationType verificationType) {
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

}
