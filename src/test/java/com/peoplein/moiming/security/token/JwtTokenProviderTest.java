package com.peoplein.moiming.security.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.support.TestMockCreator;
import com.peoplein.moiming.support.TestModelParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Date;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest extends TestMockCreator {

    // Security Config 에서 주입했으므로 직접 생성
    private MoimingTokenProvider tokenProvider = new JwtTokenProvider();



    @Test
    void generateToken_should_return_jwt_token_when_right_info_passed() {

        //given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole(1L, RoleType.USER));

        //when
        String at = tokenProvider.generateToken(MoimingTokenType.JWT_AT, mockMember);
        String rt = tokenProvider.generateToken(MoimingTokenType.JWT_RT, mockMember);

        //then
        assertTrue(StringUtils.hasText(at));
        assertThat(StringUtils.hasText(rt));

    }

    @Test
    void generateToken_should_throw_null_when_member_not_given() {
        //given
        //when
        //then
        assertThatThrownBy(() -> tokenProvider.generateToken(MoimingTokenType.JWT_AT, null))
                .isInstanceOf(NullPointerException.class);

    }


    @Test
    void verifyMemberEmail_should_return_email_when_right_token_passed() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole(1L, RoleType.USER));
        String testJwtToken = createTestJwtToken(mockMember, 1000);

        // when
        String verifiedEmail = tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testJwtToken);

        // then
        assertThat(verifiedEmail).isEqualTo(mockMember.getMemberEmail());
    }


    @Test
    void verifyMemberEmail_should_throw_expired_exception_when_token_expires() throws Exception {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole(1L, RoleType.USER));
        String testJwtToken = createTestJwtToken(mockMember, 1000);

        // when
        Thread.sleep(2000);

        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testJwtToken)).isInstanceOf(TokenExpiredException.class);

    }

    @Test
    void verifyMemberEmail_should_throw_decode_exception_when_token_null() {

        // given
        String testToken = null;

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testToken))
                .isInstanceOf(JWTDecodeException.class);

    }

    @Test
    void verifyMemberEmail_should_throw_decode_exception_when_token_empty() {

        // given
        String testToken = "";

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testToken))
                .isInstanceOf(JWTDecodeException.class);

    }

    @Test
    void verifyMemberEmail_should_throw_decode_exception_when_wrong_token_given() {

        // given
        String testToken = "NOT_JWT_TOKEN";

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testToken))
                .isInstanceOf(JWTDecodeException.class);

    }

}