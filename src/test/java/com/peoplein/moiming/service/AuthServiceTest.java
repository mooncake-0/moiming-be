package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.requesta.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;


/*
 Test 예외
 - signIn() Param 예외 -> DTO 예외는 Validation 에서 Check - Controller 단에서 진행
 - signIn() -> 죄다 외부 진행. Pass Case 외 할게 없음
 - provideToken() Member Null 예외 -> signInMember 이기 때문에 생성 실패 or save 실패에서 다 잡힌다
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest extends TestMockCreator {

    @Spy // Test Service 내 private 함수들 제어를 위함
    @InjectMocks
    private AuthService authService;

    @Spy // 진짜 일 수행시키기 위함. 구현체 주입
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OldMoimingTokenProvider oldMoimingTokenProvider;

    @Test
    void checkEmailAvailable_should_throw_error_when_used_email() {
        // given
        String email = memberEmail;
        Role mockRole = mockRole(1L, RoleType.USER);
        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(mockMember(1L, email, memberName, memberPhone, mockRole)));

        // when
        // then
        assertThatThrownBy(() -> authService.checkEmailAvailable(email)).isInstanceOf(MoimingApiException.class);
    }

    @Test
    void checkEmailAvailable_should_pass_when_unused_email() {
        // given
        String email = memberEmail;
        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.empty());

        //when
        //then
        assertDoesNotThrow(() -> authService.checkEmailAvailable(email));

    }


    @Test
    void signIn_should_create_account_when_right_info_passed() {

        // given
        MemberSignInReqDto requestDto = mockSigninRequestDto();

        //  - stubs
        doNothing().when(authService).checkUniqueColumnDuplication(any(), any());
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(mockRole(1L, RoleType.USER));
        doNothing().when(memberRepository).save(any()); // save() 함수가 반환하는게 없으므로
        doReturn(accessToken).when(authService).provideTokenByMember(any());

        // when
        TokenTransmitter<MemberSignInRespDto> response = authService.signIn(requestDto);
        MemberSignInRespDto responseDto = response.getData();

        // then assert
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(responseDto.getMemberEmail()).isEqualTo(memberEmail);
        assertThat(responseDto.getMemberName()).isEqualTo(memberName);
        assertThat(responseDto.getFcmToken()).isEqualTo(fcmToken);

        // then verify
        verify(authService, times(1)).checkUniqueColumnDuplication(any(), any());
        verify(authService, times(1)).provideTokenByMember(any());

    }


    @Test
    void checkUniqueColumnDuplication_should_throw_error_when_email_duplicates() {
        // given
        String notRegisteredPhone = "01000000000";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findByEmailOrPhone(memberEmail, notRegisteredPhone)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(memberEmail, notRegisteredPhone)).isInstanceOf(MoimingApiException.class);
    }

    @Test
    void checkUniqueColumnDuplication_should_throw_error_when_phone_duplicates() {
        // given
        String notRegisteredEmail = "not@registered.com";
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole);

        List<Member> queriedMembers = new ArrayList<>();
        queriedMembers.add(mockMember);

        // given - stub
        when(memberRepository.findByEmailOrPhone(notRegisteredEmail, memberPhone)).thenReturn(queriedMembers);

        //when
        //then
        assertThatThrownBy(() -> authService.checkUniqueColumnDuplication(notRegisteredEmail, memberPhone)).isInstanceOf(MoimingApiException.class);
    }

    @Test
    void checkUniqueColumnDuplication_should_pass_when_no_duplicate() {
        // given
        List<Member> queriedMembers = new ArrayList<>();

        // given - stub
        when(memberRepository.findByEmailOrPhone(memberEmail, memberPhone)).thenReturn(queriedMembers);

        //when
        //then
        assertDoesNotThrow(()->authService.checkUniqueColumnDuplication(memberEmail, memberPhone));
    }


    @Test
    void provideTokenByMember_should_return_access_token_and_set_refresh_token() {
        //given
        Role mockRole = mockRole(1L, RoleType.USER);
        Member mockMember = mockMember(1L, memberEmail, memberPhone, memberName, mockRole);
        when(oldMoimingTokenProvider.generateToken(eq(MoimingTokenType.JWT_AT), any())).thenReturn(accessToken);
        when(oldMoimingTokenProvider.generateToken(eq(MoimingTokenType.JWT_RT), any())).thenReturn(refreshToken);

        //when
        String returnData = authService.provideTokenByMember(mockMember);

        //then
        assertThat(returnData).isEqualTo(accessToken);
        assertThat(mockMember.getRefreshToken()).isEqualTo(refreshToken);
    }

}