package com.peoplein.moiming.service;


import com.peoplein.moiming.dev_support.TestModelCreator;
import com.peoplein.moiming.dev_support.TestModelParams;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.model.dto.auth.MemberSigninRequestDto;
import com.peoplein.moiming.model.dto.request_b.MemberReqDto;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import com.peoplein.moiming.model.dto.response_a.MemberRespDto;
import com.peoplein.moiming.model.inner.TokenTransmitter;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static com.peoplein.moiming.dev_support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request_b.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest extends TestModelCreator {

    @Spy
    @InjectMocks
    private AuthService authService;

    /*
     - 진짜 일을 수행시키기 위함
     */
    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MemberRepository memberRepository;


    @Test
    void when_right_info_passed_should_create_account() {

        // given
        MemberSignInReqDto requestDto = makeTestSigninRequestDto();

        //  - stubs
        doNothing().when(authService).checkUniqueColumnDuplication(requestDto.getMemberEmail(), requestDto.getMemberPhone());
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(makeMockRole(100L, RoleType.USER));
        doNothing().when(roleRepository).save(any()); // save() 함수가 반환하는게 없으므로
        when(authService.provideTokenByMember(any())).thenReturn(accessToken);

        // when
        TokenTransmitter<MemberSignInRespDto> response = authService.signIn(requestDto);
        MemberSignInRespDto responseDto = response.getData();

        // then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(responseDto.getMemberEmail()).isEqualTo(memberEmail);
        assertThat(responseDto.getMemberName()).isEqualTo(memberName);
        assertThat(responseDto.getFcmToken()).isEqualTo(fcmToken);

    }

}