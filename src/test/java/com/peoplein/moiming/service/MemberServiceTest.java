package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private MemberRepository memberRepository;


    @Test
    void confirmPw_shouldPass_whenRightInfoPassed() {

        // given
        String password = "NOT_EMPTY";
        Member member = mock(Member.class);

        // when
        memberService.confirmPw(password, member);

        // then
        verify(encoder, times(1)).matches(any(), any()); // 결과는 상관 없음

    }


    @Test
    void confirmPw_shouldThrowException_whenParameterInvalid_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> memberService.confirmPw("", null)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void changeNickname_shouldPass_whenRightInfoPassed() {

        // given
        String nickname = "NOT_EMPTY";
        Member member = mock(Member.class);

        // given - stub
        when(member.getNickname()).thenReturn("NOT_SAME_WITH_GIVEN_NICKNAME"); // 동일하지 않게 세팅
        when(memberRepository.findByNickname(any())).thenReturn(Optional.empty());

        // when
        memberService.changeNickname(nickname, member);

        // then
        verify(member, times(1)).changeNickname(any());

    }


    // PARAM NULL
    @Test
    void changeNickname_shouldThrowException_whenParameterInvalid_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> memberService.changeNickname("", null)).isInstanceOf(MoimingApiException.class);

    }


    // NICKNAME NOT CHANGED
    @Test
    void changeNickname_shouldThrowException_whenNicknameNotChange_byMoimingApiException() {

        // given
        String nickname = "NOT_EMPTY";
        Member member = mock(Member.class);

        // given - stub
        when(member.getNickname()).thenReturn(nickname); // 기존과 도일

        // when
        // then
        assertThatThrownBy(() -> memberService.changeNickname(nickname, member)).isInstanceOf(MoimingApiException.class);

    }


    // MEMBER NOT FOUND
    @Test
    void changeNickname_shouldThrowException_whenMemberNotFound_byMoimingApiException() {

        // given
        String nickname = "NOT_EMPTY";
        Member member = mock(Member.class);

        // given - stub
        when(member.getNickname()).thenReturn("NOT_SAME_WITH_GIVEN_NICKNAME");
        when(memberRepository.findByNickname(any())).thenReturn(Optional.of(mock(Member.class)));

        // when
        // then
        assertThatThrownBy(() -> memberService.changeNickname(nickname, member)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void changePw_shouldPass_whenRightInfoPassed() {

        // given
        String prePw = "NOT_EMPTY";
        String newPw = "NOT_EMPTY";
        Member member = mock(Member.class);

        // given - stub
        when(encoder.matches(any(), any())).thenReturn(true);
        when(encoder.encode(any())).thenReturn("ANY");
        // when
        memberService.changePw(prePw, newPw, member);

        // then
        verify(member, times(1)).changePassword(anyString());

    }


    // INVALID PARAM
    @Test
    void changePw_shouldThrowException_whenParameterInvalid_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> memberService.changePw("", "", null)).isInstanceOf(MoimingApiException.class);

    }


    // PW_INCORRECT
    @Test
    void changePw_shouldThrowException_whenPrePwIncorrect_byMoimingApiException() {

        // given
        String prePw = "NOT_EMPTY";
        String newPw = "NOT_EMPTY";
        Member member = mock(Member.class);

        // given - stub
        // TODO :: anyString 과 any, NULL 여부에 따라서 다른 것 같긴 하지만, 꼭 그 상황에서의 Null 까지 판단해줘야할까?
        //         그렇다면 member.getPassword() 까지 stubbing 해줘야 함. null 이든 말든 그냥 결과만 그렇다면 되는 듯?
        //         "뭐든 아무 상관 없고 결과가 이것!" 인게 중요해서 그래보인다
        when(encoder.matches(any(), any())).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> memberService.changePw(prePw, newPw, member)).isInstanceOf(MoimingApiException.class);

    }
}
