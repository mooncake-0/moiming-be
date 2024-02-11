package com.peoplein.moiming.security.token;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.support.TestMockCreator;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest extends TestMockCreator {

    // Security Config 에서 주입했으므로 직접 생성
    private MoimingTokenProvider tokenProvider = new JwtTokenProvider();


    @Test
    void generateToken_shouldReturnJwtToken_whenRightInfoPassed() {

        //given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, mockRole(1L, RoleType.USER));

        //when
        TokenDto atd = tokenProvider.generateToken(MoimingTokenType.JWT_AT, mockMember);
        TokenDto rtd = tokenProvider.generateToken(MoimingTokenType.JWT_RT, mockMember);

        //then
        assertTrue(StringUtils.hasText(atd.getJwtToken()));
        assertNotNull(atd.getExp());
        assertTrue(StringUtils.hasText(rtd.getJwtToken()));
        assertNotNull(rtd.getExp());

    }

    @Test
    void generateToken_shouldThrowNullException_whenMemberNotGiven() {
        //given
        //when
        //then
        assertThatThrownBy(() -> tokenProvider.generateToken(MoimingTokenType.JWT_AT, null)).isInstanceOf(NullPointerException.class);

    }


    @Test
    void verifyMemberEmail_shouldReturnEmail_whenRightTokenPassed() {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, mockRole(1L, RoleType.USER));
        String testJwtToken = createTestJwtToken(mockMember, 1000);

        // when
        String verifiedEmail = tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testJwtToken);

        // then
        assertThat(verifiedEmail).isEqualTo(mockMember.getMemberEmail());
    }


    @Test
    void verifyMemberEmail_shouldThrowExpiredException_whenTokenExpires() throws Exception {

        // given
        Member mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, mockRole(1L, RoleType.USER));
        String testJwtToken = createTestJwtToken(mockMember, 1000);

        // when
        Thread.sleep(2000);

        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testJwtToken)).isInstanceOf(TokenExpiredException.class);

    }

    @Test
    void verifyMemberEmail_shouldThrowDecodeException_whenTokenNull() {

        // given
        String testToken = null;

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testToken)).isInstanceOf(JWTDecodeException.class);

    }

    @Test
    void verifyMemberEmail_shouldThrowDecodeException_whenTokenEmpty() {

        // given
        String testToken = "";

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testToken)).isInstanceOf(JWTDecodeException.class);

    }

    @Test
    void verifyMemberEmail_shouldThrowDecodeException_whenWrongTokenPassed() {

        // given
        String testToken = "NOT_JWT_TOKEN";

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, testToken)).isInstanceOf(JWTDecodeException.class);

    }

}
