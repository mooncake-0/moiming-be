package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

public class MemberTest {

    PasswordEncoder passwordEncoder;
    String password;

    MemberInfo memberInfo;
    Member member;

    @BeforeEach
    void initInstance() {
        passwordEncoder = new BCryptPasswordEncoder();
        password = passwordEncoder.encode(TestUtils.password);
        member = TestUtils.initMemberAndMemberInfo();
        memberInfo = member.getMemberInfo();
    }

    /*
     TODO :: UID, Password Validation 정보에 따른 추가적 Test 필요
     */
    @Test
    void constructorSuccess() {
        // given
        String expectedPassword = TestUtils.password;
        String encryptedPassword = passwordEncoder.encode(TestUtils.password);

        // when
        Member member = Member.createMember(TestUtils.uid, encryptedPassword, memberInfo);

        // then
        assertThat(member.getUid()).isEqualTo(TestUtils.uid);
        assertThat(passwordEncoder.matches(expectedPassword, member.getPassword())).isTrue();
    }

    @Test
    void constructorFail() {
        // when + then
        assertThatThrownBy(() -> Member.createMember(null, password, memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember("", password, memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(TestUtils.uid, null, memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(TestUtils.uid, "", memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(TestUtils.uid, password, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void refreshTokenSuccess() {
        // given
        String changedToken = "NEW_REFRESH_TOKEN";
        Member member = Member.createMember(TestUtils.uid, password, memberInfo);

        // when
        member.changeRefreshToken(changedToken);

        // then
        assertThat(member.getRefreshToken()).isEqualTo(changedToken);
    }

    @Test
    void refreshSameTokenSuccess() {
        // given
        String sameToken = TestUtils.refreshToken;
        Member member = Member.createMember(TestUtils.uid, password, memberInfo);

        // when
        member.changeRefreshToken(sameToken);

        // then
        assertThat(member.getRefreshToken()).isEqualTo(sameToken);
    }

    @Test
    @DisplayName("실패 @ Refresh Token 변경")
    public void changeRefreshTokenFail() {
        // given
        String failToken1 = null;
        String failToken2 = "";

        // when + then
        assertThatThrownBy(() -> member.changeRefreshToken(failToken1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> member.changeRefreshToken(failToken2)).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void changePassword() {
        // given
        String inputPassword = "CHANGED_PASSWORD";

        // when
        member.changePassword(passwordEncoder.encode(inputPassword));

        // then
        assertThat(passwordEncoder.matches(inputPassword, member.getPassword())).isTrue();
    }

    @Test
    void changeSamePassword() {
        // given
        String samePassword = passwordEncoder.encode(TestUtils.password);

        // when
        member.changePassword(passwordEncoder.encode(samePassword));

        // then
        assertThat(passwordEncoder.matches(samePassword, member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("실패 @ 비밀번호 변경")
    void changePasswordException() {
        // given
        String wrongPassword1 = null;
        String wrongPassword2 = "";

        // when + then
        assertThatThrownBy(() -> member.changePassword(wrongPassword1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> member.changePassword(wrongPassword2)).isInstanceOf(IllegalArgumentException.class);
    }
}