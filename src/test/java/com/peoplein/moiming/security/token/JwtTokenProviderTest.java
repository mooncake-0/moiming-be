package com.peoplein.moiming.security.token;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import static com.peoplein.moiming.support.TestParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest extends TestObjectCreator {

    // Security Config 에서 주입했으므로 직접 생성
    private MoimingTokenProvider tokenProvider = new JwtTokenProvider();


    @Test
    void generateToken_shouldReturnJwtToken_whenRightInfoPassed() {

        //given
        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);

        //when
        TokenDto atd = tokenProvider.generateToken(MoimingTokenType.JWT_AT, testMember);
        TokenDto rtd = tokenProvider.generateToken(MoimingTokenType.JWT_RT, testMember);

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
    void verifyMemberId_shouldReturnEmail_whenRightTokenPassed() {

        //given
        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);
        String testJwtToken = createTestJwtToken(testMember, 1000);

        // when
        String verifiedMemberId = tokenProvider.verifyMemberId(MoimingTokenType.JWT_AT, testJwtToken);

        // then
        assertThat(verifiedMemberId).isEqualTo(testMember.getMemberPhone());
    }


    @Test
    void verifyMemberId_shouldThrowExpiredException_whenTokenExpires() throws Exception {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);
        String testJwtToken = createTestJwtToken(testMember, 1000);

        // when
        Thread.sleep(2000);

        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberId(MoimingTokenType.JWT_AT, testJwtToken)).isInstanceOf(TokenExpiredException.class);

    }

    @Test
    void verifyMemberId_shouldThrowDecodeException_whenTokenNull() {

        // given
        String testToken = null;

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberId(MoimingTokenType.JWT_AT, testToken)).isInstanceOf(JWTDecodeException.class);

    }

    @Test
    void verifyMemberId_shouldThrowDecodeException_whenTokenEmpty() {

        // given
        String testToken = "";

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberId(MoimingTokenType.JWT_AT, testToken)).isInstanceOf(JWTDecodeException.class);

    }

    @Test
    void verifyMemberId_shouldThrowDecodeException_whenWrongTokenPassed() {

        // given
        String testToken = "NOT_JWT_TOKEN";

        // when
        // then
        assertThatThrownBy(() -> tokenProvider.verifyMemberId(MoimingTokenType.JWT_AT, testToken)).isInstanceOf(JWTDecodeException.class);

    }

}
