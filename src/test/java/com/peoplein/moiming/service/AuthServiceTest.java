package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.TokenDto;
import com.peoplein.moiming.support.TestMockCreator;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.security.token.MoimingTokenType.*;
import static com.peoplein.moiming.support.TestModelParams.*;
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
public class AuthServiceTest extends TestMockCreator {

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
    void checkEmailAvailable_shouldThrowError_whenUsedEmail() { // 반환하는 값보단 함수 통과가 더 중요
        // given
        String email = memberEmail;
        Role mockRole = mockRole(1L, RoleType.USER);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember(1L, email, memberName, memberPhone, ci, mockRole)));

        // when
        // then
        assertDoesNotThrow(() -> authService.checkEmailAvailable(email));
    }


    @Test
    void checkEmailAvailable_shouldPass_whenUnusedEmail() {
        // given
        String email = memberEmail;
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        //then
        assertDoesNotThrow(() -> authService.checkEmailAvailable(email));

    }


    @Test
    void signIn_shouldCreateAccount_whenRightInfoPassed() {

        // given
        AuthSignInReqDto requestDto = mock(AuthSignInReqDto.class);

        // given - stub
        when(memberRepository.findMembersByEmailOrPhoneOrCi(any(), any(), any())).thenReturn(new ArrayList<>());
        when(memberRepository.findByNickname(any())).thenReturn(Optional.empty());
        // 아무 수행이나 상관 없음을 지칭
        doReturn(null).when(authService).issueTokensAndUpdateColumns(anyBoolean(), any());

        // when
        authService.signIn(requestDto);

        // then
        verify(memberRepository, times(1)).save(any());
        verify(policyAgreeService, times(1)).createPolicyAgree(any(), any());
        verify(authService, times(1)).issueTokensAndUpdateColumns(anyBoolean(), any());

    }


    @Test
    void reissueToken_shouldReissueToken_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        AuthTokenReqDto reqDto = mock(AuthTokenReqDto.class);
        String preRefreshToken = "SAVED_REFRESH_TOKEN";

        // given - stub
        when(reqDto.getToken()).thenReturn(preRefreshToken); // 리프레시 토큰 통과
        when(memberRepository.findByEmail(any())).thenReturn(Optional.of(member));
        when(member.getRefreshToken()).thenReturn(preRefreshToken);
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
        AuthTokenReqDto reqDto = mockTokenReqDto(refreshToken);

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(memberEmail)).thenReturn(Optional.empty()); // 아무것도 찾지 못했을 경우

        // when
        // then // Exception 이 제대로 터지는지 확인한다
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void reissueToken_shouldRemoveSavedTokenAndThrowException_whenTokenNotMatch_byMoimingAuthApiException() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, ci, mockRole(1L, RoleType.USER));
        AuthTokenReqDto reqDto = mockTokenReqDto("DIFF" + refreshToken); // member email 은 추출할 수 있도록 payload 부분을 건드리진 않는다

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(mockMember)); // Mocked Member 반환

        // when
        // then
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingAuthApiException.class);
        assertThat(mockMember.getRefreshToken()).isEmpty(); // REFRESH TOKEN 값을 삭제했음을 검증한다

    }


    @Test
    void reissueToken_shouldThrowException_whenMemberNotHaveToken_byMoimingAuthApiException() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, ci, mockRole(1L, RoleType.USER));
        mockMember.changeRefreshToken(""); // Member에 저장된 RT가 없음

        AuthTokenReqDto reqDto = mockTokenReqDto(refreshToken);

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(mockMember)); // Mocked Member 반환

        // when
        // then
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingAuthApiException.class);
        assertThat(mockMember.getRefreshToken()).isEmpty();

    }


    @Test
    void checkUniqueColumnDuplication_shouldThrowException_whenEmailDuplicates_byMoimingAuthApiException() {
        // given
        String notRegisteredPhone = "01000000000";
        String notRegisteredCi = "not-registered";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findMembersByEmailOrPhoneOrCi(memberEmail, notRegisteredPhone, notRegisteredCi)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(memberEmail, notRegisteredPhone, notRegisteredCi)).isInstanceOf(MoimingAuthApiException.class);
    }


    @Test
    void checkUniqueColumnDuplication_shouldThrowException_whenPhoneDuplicates_byMoimingAuthApiException() {
        // given
        String notRegisteredEmail = "not@registered.com";
        String notRegisteredCi = "not-registered";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findMembersByEmailOrPhoneOrCi(notRegisteredEmail, memberPhone, notRegisteredCi)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(notRegisteredEmail, memberPhone, notRegisteredCi)).isInstanceOf(MoimingAuthApiException.class);
    }


    @Test
    void checkUniqueColumnDuplication_shouldThrowException_whenCiDuplicates_byMoimingAuthApiException() {

        // given
        String notRegisteredPhone = "01000000000";
        String notRegisteredEmail = "not@registered.com";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findMembersByEmailOrPhoneOrCi(notRegisteredEmail, notRegisteredPhone, ci)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(notRegisteredEmail, notRegisteredPhone, ci)).isInstanceOf(MoimingAuthApiException.class);
    }


    @Test
    void checkUniqueColumnDuplication_shouldPass_whenNoDuplicate() {
        // given
        List<Member> queriedMembers = new ArrayList<>();

        // given - stub
        when(memberRepository.findMembersByEmailOrPhoneOrCi(memberEmail, memberPhone, ci)).thenReturn(queriedMembers);

        //when
        //then
        assertDoesNotThrow(() -> authService.checkUniqueColumnDuplication(memberEmail, memberPhone, ci)); // void returns
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
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, ci, mockRole(1L, RoleType.USER));

        // given - stub
        when(memberRepository.findByNickname(any())).thenReturn(Optional.ofNullable(mockMember)); // 계속 중복되는 닉네임이 있음

        // when
        // then
        assertThatThrownBy(() -> authService.tryCreateNicknameForUser()).isInstanceOf(MoimingAuthApiException.class);
    }


    // findMemberEmail
    // 성공
    @Test
    void findMemberEmail_shouldPass_whenRightInfoPassed() {

        // given
        AuthFindIdReqDto reqDto = mock(AuthFindIdReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);
        Member member = mock(Member.class);
        when(reqDto.getMemberPhone()).thenReturn("01012345678");
        when(smsVerification.getMemberPhoneNumber()).thenReturn("01012345678");

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));

        // when
        authService.findMemberEmail(reqDto);

        // then
        verify(member, times(1)).getMaskedEmail();

    }


    // 실패 - memberPhoneNotMatch
    @Test
    void findMemberEmail_shouldThrowException_whenMemberPhoneNotMatch_byMoimingAuthApiException() {

        // given
        AuthFindIdReqDto reqDto = mock(AuthFindIdReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);
        when(reqDto.getMemberPhone()).thenReturn("01000000000");
        when(smsVerification.getMemberPhoneNumber()).thenReturn("01012345678");

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);

        // when
        // then
        assertThatThrownBy(() -> authService.findMemberEmail(reqDto)).isInstanceOf(MoimingAuthApiException.class);

    }


    // 실패 - member Not Found
    @Test
    void findMemberEmail_shouldThrowException_whenMemberInSmsNotFound_byMoimingAuthApiExcetpion() {

        // given
        AuthFindIdReqDto reqDto = mock(AuthFindIdReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);
        when(reqDto.getMemberPhone()).thenReturn("01012345678");
        when(smsVerification.getMemberPhoneNumber()).thenReturn("01012345678");

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);
        when(memberRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> authService.findMemberEmail(reqDto)).isInstanceOf(MoimingApiException.class);

    }


    // confirmResetPassword
    // 성공
    @Test
    void confirmResetPassword_shouldPass_whenRightInfoPassed() {

        // given
        AuthResetPwConfirmReqDto reqDto = mock(AuthResetPwConfirmReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);
        when(smsVerification.getMemberPhoneNumber()).thenReturn("01012345678");
        when(reqDto.getMemberPhone()).thenReturn("01012345678");

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);

        // when
        // then
        assertDoesNotThrow(() -> authService.confirmResetPassword(reqDto));

    }


    // 실패 - memberPhoneNotMatch
    @Test
    void confirmResetPassword_shouldThrowException_whenMemberPhoneNotMatch_byMoimingAuthApiException() {

        // given
        AuthResetPwConfirmReqDto reqDto = mock(AuthResetPwConfirmReqDto.class);
        SmsVerification smsVerification = mock(SmsVerification.class);
        when(smsVerification.getMemberPhoneNumber()).thenReturn("01012345678");
        when(reqDto.getMemberPhone()).thenReturn("01000000000");

        // given - stub
        when(smsVerificationService.getVerifiedSmsVerification(any(), any(), any())).thenReturn(smsVerification);

        // when
        // then
        assertThatThrownBy(() -> authService.confirmResetPassword(reqDto)).isInstanceOf(MoimingAuthApiException.class);

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