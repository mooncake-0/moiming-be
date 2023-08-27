package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingInvalidTokenException;
import com.peoplein.moiming.model.dto.request.TokenReqDto;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
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

import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;
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

    @Spy // 진짜 일 수행시키기 위함. 구현체 주입 // signIn 할 때 필요
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MoimingTokenProvider tokenProvider;


    @Test
    void checkEmailAvailable_shouldThrowError_whenUsedEmail() {
        // given
        String email = memberEmail;
        Role mockRole = mockRole(1L, RoleType.USER);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember(1L, email, memberName, memberPhone, mockRole)));

        // when
        // then
        assertThatThrownBy(() -> authService.checkEmailAvailable(email)).isInstanceOf(MoimingApiException.class);
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
        MemberSignInReqDto requestDto = mockSigninReqDto(); // VALIDATION Controller 단에서 컷

        // given - stubs
        doNothing().when(authService).checkUniqueColumnDuplication(any(), any()); // 정상 객체 authService 안에서 일부를 mocking 한다 - Spy
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(mockRole(1L, RoleType.USER)); // role 이 null 이 되면 안되므로
        doNothing().when(memberRepository).save(any()); // save() 함수가 반환하는게 없으므로
        doReturn(accessToken).when(authService).issueJwtTokens(any()); // 정상 객체 authService 안에서 일부를 mocking 한다 - Spy
        doReturn(nickname).when(authService).tryCreateNicknameForUser();

        // when
        Map<String, Object> transmit = authService.signIn(requestDto);
        MemberSignInRespDto responseData = (MemberSignInRespDto) transmit.get(authService.KEY_RESPONSE_DATA);

        // then - assert
        assertThat(transmit.get(authService.KEY_ACCESS_TOKEN)).isEqualTo(accessToken);
        assertThat(responseData.getMemberEmail()).isEqualTo(memberEmail);
        assertThat(responseData.getMemberInfo().getMemberName()).isEqualTo(memberName);
        assertThat(responseData.getFcmToken()).isEqualTo(fcmToken);
        assertThat(responseData.getNickname()).isEqualTo(nickname);

        // then - verify
        verify(authService, times(1)).checkUniqueColumnDuplication(any(), any());
        verify(authService, times(1)).issueJwtTokens(any());

    }


    @Test
    void reissueToken_shouldReissueToken_whenRightInfoPassed() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole(1L, RoleType.USER));
        TokenReqDto reqDto = mockTokenReqDto(refreshToken);

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(MoimingTokenType.JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(mockMember));

        // given - stub - signIn 과는 다르게 issueJwtToken() 일부 정상 동작 필요
        when(tokenProvider.generateToken(eq(MoimingTokenType.JWT_AT), any())).thenReturn("NEW_ACCESS_TOKEN");
        when(tokenProvider.generateToken(eq(MoimingTokenType.JWT_RT), any())).thenReturn("NEW_REFRESH_TOKEN");

        //when
        Map<String, Object> transmit = authService.reissueToken(reqDto);
        TokenRespDto respDto = (TokenRespDto) transmit.get(authService.KEY_RESPONSE_DATA);

        //then
        assertThat(transmit.get(authService.KEY_ACCESS_TOKEN)).isEqualTo("NEW_ACCESS_TOKEN");
        assertThat(respDto.getRefreshToken()).isEqualTo("NEW_REFRESH_TOKEN");
        assertThat(mockMember.getRefreshToken()).isEqualTo("NEW_REFRESH_TOKEN");

        //then - verify 검증할 것 없음
    }


    @Test
    void reissueToken_shouldThrowException_whenUserNotFound() {

        // given
        TokenReqDto reqDto = mockTokenReqDto(refreshToken);

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(MoimingTokenType.JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(memberEmail)).thenReturn(Optional.empty()); // 아무것도 찾지 못했을 경우

        // when
        // then // Exception 이 제대로 터지는지 확인한다
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void reissueToken_shouldRemoveSavedTokenAndThrowException_whenTokenNotMatch() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, mockRole(1L, RoleType.USER));
        TokenReqDto reqDto = mockTokenReqDto("DIFF" + refreshToken); // member email 은 추출할 수 있도록 payload 부분을 건드리진 않는다

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(MoimingTokenType.JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(mockMember)); // Mocked Member 반환

        // when
        // then
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingInvalidTokenException.class);
        assertThat(mockMember.getRefreshToken()).isEmpty(); // REFRESH TOKEN 값을 삭제했음을 검증한다

    }


    @Test
    void reissueToken_shouldThrowException_whenMemberNotHaveToken() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, mockRole(1L, RoleType.USER));
        mockMember.changeRefreshToken(""); // Member에 저장된 RT가 없음

        TokenReqDto reqDto = mockTokenReqDto(refreshToken);

        // given - stub
        when(tokenProvider.verifyMemberEmail(eq(MoimingTokenType.JWT_RT), any())).thenReturn(memberEmail);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.ofNullable(mockMember)); // Mocked Member 반환

        // when
        // then
        assertThatThrownBy(() -> authService.reissueToken(reqDto)).isInstanceOf(MoimingInvalidTokenException.class);
        assertThat(mockMember.getRefreshToken()).isEmpty();

    }


    @Test
    void checkUniqueColumnDuplication_shouldThrowException_whenEmailDuplicates() {
        // given
        String notRegisteredPhone = "01000000000";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findMembersByEmailOrPhone(memberEmail, notRegisteredPhone)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(memberEmail, notRegisteredPhone)).isInstanceOf(MoimingApiException.class);
    }


    @Test
    void checkUniqueColumnDuplication_shouldThrowException_whenPhoneDuplicates() {
        // given
        String notRegisteredEmail = "not@registered.com";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findMembersByEmailOrPhone(notRegisteredEmail, memberPhone)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(notRegisteredEmail, memberPhone)).isInstanceOf(MoimingApiException.class);
    }


    @Test
    void checkUniqueColumnDuplication_shouldPass_whenNoDuplicate() {
        // given
        List<Member> queriedMembers = new ArrayList<>();

        // given - stub
        when(memberRepository.findMembersByEmailOrPhone(memberEmail, memberPhone)).thenReturn(queriedMembers);

        //when
        //then
        assertDoesNotThrow(() -> authService.checkUniqueColumnDuplication(memberEmail, memberPhone)); // void returns
    }


    @Test
    void issueJwtTokens_shouldReturnAccessTokenAndSetRefreshToken_whenRightInfoPassed() {
        //given
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, mockRole);
        when(tokenProvider.generateToken(eq(MoimingTokenType.JWT_AT), any())).thenReturn(accessToken); // stubbing 시 하나만 Matcher 넣는 것은 불가능 // All Params Matcher or 실제 Data
        when(tokenProvider.generateToken(eq(MoimingTokenType.JWT_RT), any())).thenReturn(refreshToken);

        //when
        String returnData = authService.issueJwtTokens(mockMember);

        //then
        assertThat(returnData).isEqualTo(accessToken);
        assertThat(mockMember.getRefreshToken()).isEqualTo(refreshToken);
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
    void tryCreateNicknameForUser_shouldThrowException_whenDuplicated10Times() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, mockRole(1L, RoleType.USER));

        // given - stub
        when(memberRepository.findByNickname(any())).thenReturn(Optional.ofNullable(mockMember)); // 계속 중복되는 닉네임이 있음

        // when
        // then
        assertThatThrownBy(() -> authService.tryCreateNicknameForUser()).isInstanceOf(MoimingApiException.class);
    }


}