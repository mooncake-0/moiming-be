package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.security.token.MoimingTokenType.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/*
 Test 예외
 - signIn() Param 예외 -> DTO 예외는 Validation 에서 Check - Controller 단에서 진행
 - signIn() -> 죄다 외부 진행. Pass Case 외 할게 없음
 - provideToken() Member Null 예외 -> signInMember 이기 때문에 생성 실패 or save 실패에서 다 잡힌다
 - reissueToken() -> 외부 진행말고 발생할 Exception 검증 완료 / TokenExpire 등은 token provider 에서 검증함
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest extends TestObjectCreator {

    @Spy // Test Service 내 private 함수들 제어를 위함
    @InjectMocks
    private AuthService authService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MoimingTokenProvider tokenProvider;

    @Mock
    private PolicyAgreeService policyAgreeService;

    @Mock
    private SmsVerificationService smsVerificationService;


    @Test
    void signUp_shouldCreateAccount_whenRightInfoPassed() {

        // given
        AuthSignUpReqDto requestDto = mock(AuthSignUpReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsVerificationService.confirmAndGetValidSmsVerification(any(), any())).thenReturn(smsVerification);
        when(smsVerification.getMemberPhoneNumber()).thenReturn("SAME_PHONE_NUMBER");
        when(requestDto.getMemberPhone()).thenReturn("SAME_PHONE_NUMBER"); // SMS 단순 검증 통과
        when(memberRepository.findByNickname(any())).thenReturn(Optional.empty()); // 중복 닉네임 없음 통과

        // 아무 수행이나 상관 없음을 지칭
        doReturn(null).when(authService).issueTokensAndUpdateColumns(anyBoolean(), any());

        // when
        authService.signUp(requestDto);

        // then
        verify(memberRepository, times(1)).save(any());
        verify(policyAgreeService, times(1)).createPolicyAgree(any(), any());
        verify(authService, times(1)).issueTokensAndUpdateColumns(anyBoolean(), any());

    }


    @Test
    void signUp_shouldThrowException_whenReqPhoneNumberAndVerificationPhoneNumberNotMatch_byMoimingAuthApiException() {

        // given
        AuthSignUpReqDto requestDto = mock(AuthSignUpReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsVerificationService.confirmAndGetValidSmsVerification(any(), any())).thenReturn(smsVerification);
        when(smsVerification.getMemberPhoneNumber()).thenReturn("SAME_PHONE_NUMBER");
        when(requestDto.getMemberPhone()).thenReturn("NOT_SAME_PHONE_NUMBER"); // NOT MATCH

        // when
        // then
        assertThatThrownBy(() -> authService.signUp(requestDto)).isInstanceOf(MoimingAuthApiException.class);
        verify(memberRepository, times(0)).save(any());
        verify(policyAgreeService, times(0)).createPolicyAgree(any(), any());
        verify(authService, times(0)).issueTokensAndUpdateColumns(anyBoolean(), any());

    }


    @Test
    void reissueToken_shouldReissueToken_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        AuthTokenReqDto reqDto = mock(AuthTokenReqDto.class);
        String preRefreshToken = "SAVED_REFRESH_TOKEN";

        // given - stub
        when(reqDto.getToken()).thenReturn(preRefreshToken); // 리프레시 토큰 통과
        when(memberRepository.findByMemberPhone(any())).thenReturn(Optional.of(member)); // 조회되는 유저 Id
        when(member.getRefreshToken()).thenReturn(preRefreshToken); // 리프레시 토큰 일치

        // 현재 함수는 이 함수와 분리되어야 한다
        doReturn(null).when(authService).issueTokensAndUpdateColumns(anyBoolean(), any());

        //when
        authService.reissueToken(reqDto);

        // then
        verify(authService, times(1)).issueTokensAndUpdateColumns(anyBoolean(), any());

    }


    @Test
    void reissueToken_shouldThrowException_whenUserNotFound_byMoimingApiException() {

        // given
        AuthTokenReqDto reqDto = new AuthTokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(refreshToken);

        // given - stub
        when(tokenProvider.verifyMemberId(eq(JWT_RT), any())).thenReturn(memberPhone);
        when(memberRepository.findByMemberPhone(any())).thenReturn(Optional.empty()); // 아무 유저도 찾지 못했을 경우

        // when
        // then // Exception 이 제대로 터지는지 확인한다
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void reissueToken_shouldRemoveSavedTokenAndThrowException_whenTokenNotMatch_byMoimingAuthApiException() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);
        testMember.changeRefreshToken(refreshToken);

        AuthTokenReqDto reqDto = new AuthTokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken("DIFF_" + refreshToken); // 다른 Refresh Token 으로 요청한다

        // given - stub
        when(tokenProvider.verifyMemberId(eq(JWT_RT), any())).thenReturn(memberPhone);
        when(memberRepository.findByMemberPhone(any())).thenReturn(Optional.of(testMember)); // Mocked Member 반환

        // when
        // then
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingAuthApiException.class);
        assertThat(testMember.getRefreshToken()).isEmpty(); // REFRESH TOKEN 값을 삭제했음을 검증한다

    }


    @Test
    void reissueToken_shouldThrowException_whenMemberNotHaveToken_byMoimingAuthApiException() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);
        testMember.changeRefreshToken(""); // Member에 저장된 RT가 없음

        AuthTokenReqDto reqDto = new AuthTokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(refreshToken); // 다른 Refresh Token 으로 요청한다

        // given - stub
        when(tokenProvider.verifyMemberId(eq(JWT_RT), any())).thenReturn(memberPhone);
        when(memberRepository.findByMemberPhone(any())).thenReturn(Optional.of(testMember)); // Mocked Member 반환

        // when
        // then
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingAuthApiException.class);
        assertThat(testMember.getRefreshToken()).isEmpty();

    }


    @Test
    void tryCreateNicknameForUser_shouldReturnNickname() {

        // given
        // given - stub
        when(memberRepository.findByNickname(any())).thenReturn(Optional.empty());

        // when
        String createdNickname = authService.tryCreateNicknameForUser();

        // then
        assertTrue(StringUtils.hasText(createdNickname));

    }


    @Test
    void tryCreateNicknameForUser_shouldThrowException_whenDuplicated10Times_byMoiminAuthApiException() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);

        // given - stub
        when(memberRepository.findByNickname(any())).thenReturn(Optional.ofNullable(testMember)); // 계속 중복되는 닉네임이 있음

        // when
        // then
        assertThatThrownBy(() -> authService.tryCreateNicknameForUser()).isInstanceOf(MoimingAuthApiException.class);
    }


    // confirmResetPassword
    // 성공
    @Test
    void verifySmsVerification_shouldPass_whenRightInfoPassed() {

        // given
        Long smsVerificationId = 1L; // 상관없음, ANY
        String verificationNumber = "123456"; // 상관없음, ANY
        VerificationType type = VerificationType.SIGN_UP; // 상관 없음 // ANY
        String memberPhone = "01012345678";
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);
        when(smsVerification.getMemberPhoneNumber()).thenReturn(memberPhone);

        // when
        // then
        assertDoesNotThrow(() -> authService.verifySmsVerification(smsVerificationId, type, memberPhone, verificationNumber));

    }


    // 실패 - memberPhoneNotMatch
    @Test
    void verifySmsVerification_shouldThrowException_whenMemberPhoneNotMatch_byMoimingAuthApiException() {

        // given
        Long smsVerificationId = 1L; // 상관없음, ANY
        String verificationNumber = "123456"; // 상관없음, ANY
        VerificationType type = VerificationType.SIGN_UP; // 상관 없음 // ANY
        String memberPhone = "01012345678";
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);
        when(smsVerification.getMemberPhoneNumber()).thenReturn("01000000000"); // 다름

        // when
        // then
        assertThatThrownBy(() -> authService.verifySmsVerification(smsVerificationId, type, memberPhone, verificationNumber)).isInstanceOf(MoimingAuthApiException.class);

    }


    // resetPassword
    // 성공
    @Test
    void resetPassword_shouldPassAndVerifyMethods_whenRightInfoPassed() {

        // given
        AuthResetPwReqDto reqDto = mock(AuthResetPwReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);
        Member member = mock(Member.class);

        // given - stub
        when(smsVerificationService.confirmAndGetValidSmsVerification(any(), any())).thenReturn(smsVerification);
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));

        // when
        authService.resetPassword(reqDto);

        // then
        verify(passwordEncoder, times(1)).encode(any());
        verify(member, times(1)).changePassword(any());

    }


    // 실패 - member Not Found
    @Test
    void resetPassword_shouldThrowException_whenMemberInVerificationNotFound_byMoimingAuthApiException() {

        // given
        AuthResetPwReqDto reqDto = mock(AuthResetPwReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);

        // given - stub
        when(smsVerificationService.confirmAndGetValidSmsVerification(any(), any())).thenReturn(smsVerification);
        when(memberRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> authService.resetPassword(reqDto)).isInstanceOf(MoimingApiException.class);

    }


}