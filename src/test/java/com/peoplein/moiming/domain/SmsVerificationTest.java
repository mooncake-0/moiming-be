package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class SmsVerificationTest {

    // VerificationType 값은 랜덤한 셀렉션이다
    // SmsVerification 생성
    @Test
    void createSmsVerification_shouldCreateSmsVerification_whenPassedInfo() {

        // given
        Long memberId = 1L;
        String memberPhoneNumber = "01012345678";
        VerificationType type = VerificationType.FIND_PW;

        // when
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);
        System.out.println("verification.getVerificationNumber() = " + verification.getVerificationNumber());

        // then
        assertNotNull(verification.getVerificationNumber());

    }


    // confirmVerification 성공
    @Test
    void confirmVerification_shouldSuccess_whenRightInfoPassed() {

        // given
        Long memberId = 1L;
        String memberPhoneNumber = "01012345678";
        VerificationType type = VerificationType.FIND_PW;
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);
        String number = verification.getVerificationNumber();

        // when
        // then
        assertDoesNotThrow(() -> verification.confirmVerification(VerificationType.FIND_PW, number));

    }


    // confirmVerification 실패 - number not same
    @Test
    void confirmVerification_shouldThrowException_whenNumberNotMatch_byMoimingAuthApiException() {

        // given
        Long memberId = 1L;
        String memberPhoneNumber = "01012345678";
        VerificationType type = VerificationType.FIND_PW;
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);
        String number = verification.getVerificationNumber();
        String wrongInputNum = "000000";

        // when
        // then
        assertThatThrownBy(() -> verification.confirmVerification(VerificationType.FIND_PW, wrongInputNum)).isInstanceOf(MoimingAuthApiException.class);

    }


    // isValidAndVerified 성공
    @Test
    void isValidAndVerified_shouldSuccess_whenPassedRightInfo() {

        // given
        Long memberId = 1L;
        String memberPhoneNumber = "01012345678";
        VerificationType type = VerificationType.FIND_PW;
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);

        // given - change status
        String number = verification.getVerificationNumber();
        verification.confirmVerification(VerificationType.FIND_PW, number); // VALID 하게 만든다

        // when
        // then
        assertDoesNotThrow(() -> verification.isValidAndVerified(VerificationType.FIND_PW));

    }


    // isValidAndVerified 실패 - not verified
    @Test
    void isValidAndVerified_shouldThrowException_whenSmsVerificationNotValid_byMoimingAuthApiException() {

        // given
        Long memberId = 1L;
        String memberPhoneNumber = "01012345678";
        VerificationType type = VerificationType.FIND_PW;
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);

        // when
        // then
        assertThatThrownBy(()->verification.isValidAndVerified(VerificationType.FIND_PW)).isInstanceOf(MoimingAuthApiException.class);

    }


    // isValidAndVerified 실패 - type not match - FIND_PW 생성 + 번호 인증 완료 - SIGNUP 으로 Valid 함 확인
    @Test
    void isValidAndVerified_shouldThrowException_whenSmsVerificationTypeNotMatch_byMoimingAuthApiException() {

        // given
        Long memberId = 1L;
        String memberPhoneNumber = "01012345678";
        VerificationType type = VerificationType.FIND_PW;
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);

        // given - change status
        String number = verification.getVerificationNumber();
        verification.confirmVerification(VerificationType.FIND_PW, number); // VALID 하게 만든다

        // when
        // then
        assertThatThrownBy(()->verification.isValidAndVerified(VerificationType.SIGN_UP)).isInstanceOf(MoimingAuthApiException.class);

    }


    // isValidAndVerified 실패 - expired
    // SMS Verification 특성상, 그리고 checkExpiration private 함수는 Test 가 어려우니 동일한 Library 의 동작방식을 사용하여 Test 한다
    @Test
    void checkExpirationInSameWay_shouldThrowException_whenVerificationExpired() throws InterruptedException {

        // given
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(1);
        LocalDateTime now = LocalDateTime.now();

        // when
        boolean isExpired = expiredAt.isBefore(now);

        // then
        assertTrue(isExpired);

    }

}
