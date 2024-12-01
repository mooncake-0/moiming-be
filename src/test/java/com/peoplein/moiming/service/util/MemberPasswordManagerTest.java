package com.peoplein.moiming.service.util;

import com.peoplein.moiming.exception.MoimingApiException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

// 유지 보수성, 객체 지향성보단
// 역할 수행 자체가 더 중요 - 결과 기반 TEST 진행
public class MemberPasswordManagerTest {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    private MemberPasswordManager pwManager = new MemberPasswordManager(encoder);

    @Test
    void encodePwWhenCreate_shouldPass_whenConsistsEngAndNumbLength8() {

        // given
        String pw = "1234qwer";

        // when
        String encoded = pwManager.encodePwWhenCreate(pw);

        // then
        assertTrue(encoder.matches(pw, encoded));
    }

    @Test
    void encodePwWhenCreate_shouldPass_whenConsistsEngAndSpecLength9() {

        // given
        String pw = "qw!er@\"|.";

        // when
        String encoded = pwManager.encodePwWhenCreate(pw);

        // then
        assertTrue(encoder.matches(pw, encoded));
    }

    @Test
    void encodePwWhenCreate_shouldPass_whenConsistsEngAndNumAndSpecLength11() {

        // given
        String pw = "q1w2e@3r4<{";

        // when
        String encoded = pwManager.encodePwWhenCreate(pw);

        // then
        assertTrue(encoder.matches(pw, encoded));
    }

    // EF
    // 8자 미만
    @Test
    void encodePwWhenCreate_shouldThrowException_whenLowerThan8() {

        // given
        String pw = "R!blt8";

        // when
        // then
        assertThatThrownBy(() -> pwManager.encodePwWhenCreate(pw)).isInstanceOf(MoimingApiException.class);

    }

    // 20자 초과
    @Test
    void encodePwWhenCreate_shouldThrowException_whenHigherThan20() {

        // given
        String pw = "R!ghtP@sswordButHigherThan!!";

        // when
        // then
        assertThatThrownBy(() -> pwManager.encodePwWhenCreate(pw)).isInstanceOf(MoimingApiException.class);

    }

    // 영어 없음
    @Test
    void encodePwWhenCreate_shouldThrowException_whenNoEng() {

        // given
        String pw = "1234!@#$";

        // when
        // then
        assertThatThrownBy(() -> pwManager.encodePwWhenCreate(pw)).isInstanceOf(MoimingApiException.class);

    }

    // 영어로만 되어 있음
    @Test
    void encodePwWhenCreate_shouldThrowException_whenOnlyEng() {

        // given
        String pw = "onlyenglish";

        // when
        // then
        assertThatThrownBy(() -> pwManager.encodePwWhenCreate(pw)).isInstanceOf(MoimingApiException.class);

    }

    // 한글임
    @Test
    void encodePwWhenCreate_shouldThrowException_whenKor() {

        // given
        String pw = "갑자기한글비밀번호";

        // when
        // then
        assertThatThrownBy(() -> pwManager.encodePwWhenCreate(pw)).isInstanceOf(MoimingApiException.class);

    }
}