package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.BadAuthParameterInputException;
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
        Role role = new Role(1L, "admin", RoleType.ADMIN);

        // when
        Member member = Member.createMember(TestUtils.uid, encryptedPassword, TestUtils.memberEmail,
                TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role);

        // then
        assertThat(member.getUid()).isEqualTo(TestUtils.uid);
        assertThat(passwordEncoder.matches(expectedPassword, member.getPassword())).isTrue();
        assertThat(role.getRoleType()).isEqualTo(role.getRoleType());
        assertThat(member.getMemberInfo().getMemberName()).isEqualTo(TestUtils.memberName);
    }

    @Test
    void constructorFail() {
        // given
        Role role = TestUtils.initAdminRole();

        // when + then
        assertThatThrownBy(() -> Member.createMember(
                null, password, TestUtils.memberEmail, TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(
                TestUtils.uid, null, TestUtils.memberEmail, TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(
                TestUtils.uid, TestUtils.encryptedPassword, null, TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role)).isInstanceOf(BadAuthParameterInputException.class);
        assertThatThrownBy(() -> Member.createMember(
                TestUtils.uid, TestUtils.encryptedPassword, TestUtils.memberEmail, null, TestUtils.fcmToken, TestUtils.memberGender, role)).isInstanceOf(BadAuthParameterInputException.class);
        assertThatThrownBy(() -> Member.createMember(
                TestUtils.uid, TestUtils.encryptedPassword, TestUtils.memberEmail, TestUtils.memberName, TestUtils.fcmToken, null, role)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(
                TestUtils.uid, TestUtils.encryptedPassword, TestUtils.memberEmail, TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void refreshTokenSuccess() {
        // given
        String changedToken = "NEW_REFRESH_TOKEN";
        Role role = new Role(1L, "admin", RoleType.ADMIN);
        Member member = Member.createMember(TestUtils.uid, TestUtils.password, TestUtils.memberEmail,
                TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role);

        // when
        member.changeRefreshToken(changedToken);

        // then
        assertThat(member.getRefreshToken()).isEqualTo(changedToken);
    }

    @Test
    void refreshSameTokenSuccess() {
        // given
        String sameToken = TestUtils.refreshToken;
        Role role = new Role(1L, "admin", RoleType.ADMIN);
        Member member = Member.createMember(TestUtils.uid, TestUtils.password, TestUtils.memberEmail,
                TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role);

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