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
import com.peoplein.moiming.service.external.SmsSender;
import com.peoplein.moiming.service.util.SmsRequestBuilder;
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
    private SmsSender smsSender;


    // 성공 - FIND ID
    @Test
    void processSmsVerification_shouldPass_whenFindingId() {

        // given
        AuthSmsReqDto requestDto = mock(AuthSmsReqDto.class);
        Member member = mock(Member.class);
        MemberInfo memberInfo = mock(MemberInfo.class);

        // given - stub, situational stub
        when(memberRepository.findWithMemberInfoByPhoneNumber(any())).thenReturn(Optional.of(member));
        when(requestDto.getVerifyType()).thenReturn(VerificationType.FIND_ID);
        when(memberInfo.getMemberName()).thenReturn("ANY_NAME");
        when(member.getMemberInfo()).thenReturn(memberInfo);
        when(requestDto.getMemberName()).thenReturn("ANY_NAME");

        // when
        smsService.processSmsVerification(requestDto);

        // then
        verify(smsRequestBuilder, times(1)).getHttpRequest(any());
        verify(smsSender, times(1)).sendMessage(any());
        verify(smsRepository, times(1)).save(any());

    }


    // 성공 - FIND PW
    @Test
    void processSmsVerification_shouldPass_whenFindingPw() {

        // given
        AuthSmsReqDto requestDto = mock(AuthSmsReqDto.class);
        Member member = mock(Member.class);
        MemberInfo memberInfo = mock(MemberInfo.class);

        // given - stub, situational stub
        when(memberRepository.findWithMemberInfoByPhoneNumber(any())).thenReturn(Optional.of(member));
        when(requestDto.getVerifyType()).thenReturn(VerificationType.FIND_PW);
        when(member.getMemberInfo()).thenReturn(memberInfo);
        when(member.getMemberEmail()).thenReturn("ANY_EMAIL");
        when(requestDto.getMemberEmail()).thenReturn("ANY_EMAIL");

        // when
        smsService.processSmsVerification(requestDto);

        // then
        verify(smsRequestBuilder, times(1)).getHttpRequest(any());
        verify(smsSender, times(1)).sendMessage(any());
        verify(smsRepository, times(1)).save(any());

    }


    // 실패 NULL
    @Test
    void processSmsVerification_shouldThrowException_whenParamsNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> smsService.processSmsVerification(null)).isInstanceOf(MoimingApiException.class);

    }


    // 실패 - Member 없음
    @Test
    void processSmsVerification_shouldThrowException_whenMemberNotFound_byMoimingApiException() {

        // given
        AuthSmsReqDto requestDto = mock(AuthSmsReqDto.class);

        // given - stub
        when(memberRepository.findWithMemberInfoByPhoneNumber(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> smsService.processSmsVerification(requestDto)).isInstanceOf(MoimingApiException.class);

    }


    // 실패 - 시도한 Member 와 전달받은 Member 가 일치하지 않음
    @Test
    void processSmsVerification_shouldThrowException_whenMemberNameNotMatch_byMoimingApiException() {

        // given
        AuthSmsReqDto requestDto = mock(AuthSmsReqDto.class);
        Member member = mock(Member.class);
        MemberInfo memberInfo = mock(MemberInfo.class);

        // given - stub, situational stub
        when(memberRepository.findWithMemberInfoByPhoneNumber(any())).thenReturn(Optional.of(member));
        when(requestDto.getVerifyType()).thenReturn(VerificationType.FIND_ID);
        when(memberInfo.getMemberName()).thenReturn("ANY_NAME");
        when(member.getMemberInfo()).thenReturn(memberInfo);
        when(requestDto.getMemberName()).thenReturn("NOT_MATCHING_NAME");

        // when
        // then
        assertThatThrownBy(() -> smsService.processSmsVerification(requestDto)).isInstanceOf(MoimingAuthApiException.class);

    }


    // 실패 - 시도한 Member Email 과 전달받은 Member Email 이 일치하지 않음
    @Test
    void processSmsVerification_shouldThrowException_whenMemberEmailNotMatchFindingPw_byMoimingApiException() {

        // given
        AuthSmsReqDto requestDto = mock(AuthSmsReqDto.class);
        Member member = mock(Member.class);

        // given - stub, situational stub
        when(memberRepository.findWithMemberInfoByPhoneNumber(any())).thenReturn(Optional.of(member));
        when(requestDto.getVerifyType()).thenReturn(VerificationType.FIND_PW);
        when(member.getMemberEmail()).thenReturn("ANY_EMAIL");
        when(requestDto.getMemberEmail()).thenReturn("NOT_MATCHING_EMAIL");

        // when
        // then
        assertThatThrownBy(() -> smsService.processSmsVerification(requestDto)).isInstanceOf(MoimingAuthApiException.class);

    }


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
