package com.peoplein.moiming.service;


import com.peoplein.moiming.support.TestMockCreator;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.model.inner.TokenTransmitter;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request_b.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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


    @Test
    void when_right_info_passed_should_create_account() {

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

}