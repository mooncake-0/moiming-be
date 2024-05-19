package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import com.peoplein.moiming.service.external.ExternalReqSender;
import com.peoplein.moiming.service.util.sms.CoolSmsRequestBuilder;
import com.peoplein.moiming.service.util.sms.NaverSmsRequestBuilder;
import com.peoplein.moiming.service.util.sms.SmsRequestBuilder;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SmsVerificationServiceTest {

    @InjectMocks
    private SmsVerificationService smsService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SmsVerificationRepository smsRepository;

    @Mock
    private SmsRequestBuilder smsRequestBuilder;

    @Mock
    private ExternalReqSender externalReqSender;


    // processSmsVerification
    // 실패 :: RequestDto Null
    @Test
    void processSmsVerification_shouldThrowException_whenRequestDtoNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> smsService.processSmsVerification(null)).isInstanceOf(MoimingApiException.class);
        verify(smsRequestBuilder, times(0)).getHttpRequest(any());
        verify(externalReqSender, times(0)).sendAsynchronousMessage(any());
        verify(smsRepository, times(0)).save(any());

    }


    // 성공 :: SIGN_UP
    @Test
    void processSmsVerification_shouldPass_whenSignUpReqNormal() {

        // given
        AuthSmsReqDto reqDto = mock(AuthSmsReqDto.class);

        // given - stub
        when(reqDto.getVerifyType()).thenReturn(VerificationType.SIGN_UP);

        // when
        smsService.processSmsVerification(reqDto);

        // then
        verify(smsRequestBuilder, times(1)).getHttpRequest(any());
        verify(externalReqSender, times(1)).sendAsynchronousMessage(any());
        verify(smsRepository, times(1)).save(any());

    }


    // 실패 :: FIND_PW - 핸드폰 번호의 유저를 찾을 수 없음
    @Test
    void processSmsVerification_shouldThrowException_whenPhoneNotFoundDuringFindPw_byMoimingApiException() {

        // given
        AuthSmsReqDto reqDto = mock(AuthSmsReqDto.class);

        // given - stub
        when(reqDto.getVerifyType()).thenReturn(VerificationType.FIND_PW);
        when(memberRepository.findByMemberPhone(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> smsService.processSmsVerification(reqDto)).isInstanceOf(MoimingApiException.class);
        verify(smsRequestBuilder, times(0)).getHttpRequest(any());
        verify(externalReqSender, times(0)).sendAsynchronousMessage(any());
        verify(smsRepository, times(0)).save(any());

    }


    // 실패 :: 알 수 없는 Type 이 들어옴 (NULL)
    // NULL 로는 발생할 리 없음. 잘못된 타입 변환은 일찍 잡힘
//    @Test
//    void processSmsVerification_shouldThrowException_whenInvalidVerifyType_byMoimingApiException() {
//
//         given
//        AuthSmsReqDto reqDto = mock(AuthSmsReqDto.class);
//
//         given - stub
//        when(reqDto.getVerifyType()).thenReturn(null); // INVALID 한 값
//
//         when
//         then
//        assertThatThrownBy(() -> smsService.processSmsVerification(reqDto)).isInstanceOf(MoimingApiException.class);
//        verify(smsRequestBuilder, times(0)).getHttpRequest(any());
//        verify(externalReqSender, times(0)).sendAsynchronousMessage(any());
//        verify(smsRepository, times(0)).save(any());
//
//    }


    // 성공 :: FIND_PW - 성공


    @Test
    void getVerifiedSmsVerification_shouldPass_whenRightInfo() {

        // given
        Long smsVerificationId = 1L;
        String verificationNumber = "123456";
        VerificationType type = mock(VerificationType.class); // 어떤 것이든 상관 없음
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsRepository.findById(any())).thenReturn(Optional.of(smsVerification));

        // when
        smsService.getVerifiedSmsVerification(smsVerificationId, type, verificationNumber);

        // then
        verify(smsVerification, times(1)).confirmVerification(any(), any());

    }


    // 실패 NULL
    @Test
    void getVerifiedSmsVerification_shouldThrowException_whenParamNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> smsService.getVerifiedSmsVerification(null, null, null)).isInstanceOf(MoimingApiException.class);

    }


    // SmsVerification 없음
    @Test
    void getVerifiedSmsVerification_shouldThrowException_whenSmsVerificationNotFound_byMoimingAuthApiException() {

        // given
        Long smsVerificationId = 1L;
        String verificationNumber = "123456";
        VerificationType type = mock(VerificationType.class); // 어떤 것이든 상관 없음
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> smsService.getVerifiedSmsVerification(smsVerificationId, type, verificationNumber)).isInstanceOf(MoimingAuthApiException.class);

    }


    // 성공
    @Test
    void confirmAndGetValidSmsVerification_shouldPass_whenRightInfo() {

        // given
        Long smsVerificationId = 1L;
        SmsVerification smsVerification = mock(SmsVerification.class);
        VerificationType type = mock(VerificationType.class); // 어떤 것이든 상관 없음


        // given - stub
        when(smsRepository.findById(any())).thenReturn(Optional.of(smsVerification));

        // when
        smsService.confirmAndGetValidSmsVerification(type, smsVerificationId);

        // then
        verify(smsVerification, times(1)).isValidAndVerified(any());
    }


    // 실패 - NULL
    @Test
    void confirmAndGetValidSmsVerification_shouldThrowException_whenParamNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> smsService.confirmAndGetValidSmsVerification(null, null)).isInstanceOf(MoimingApiException.class);

    }


    // 실패 - SMS NOT FOUND
    @Test
    void confirmAndGetValidSmsVerification_shouldThrowException_whenSmsVerificationNotFound_byMoimingApiException() {

        // given
        Long smsVerificationId = 1L;
        VerificationType type = mock(VerificationType.class); // 어떤 것이든 상관 없음

        // given - stub
        when(smsRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> smsService.confirmAndGetValidSmsVerification(type, smsVerificationId)).isInstanceOf(MoimingAuthApiException.class);

    }

}
