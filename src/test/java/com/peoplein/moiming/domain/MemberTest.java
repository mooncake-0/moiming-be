package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MemberGender;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

public class MemberTest {

    private PasswordEncoder passwordEncoder;
    private static String uid = "wrock.kang";
    private static String password;
    private static String refreshToken = "REFRESH_TOKEN";
    private static String fcmToken = "FCM_TOKEN";

    private MemberInfo memberInfo;
    private static String memberEmail = "a@moiming.net";
    private static String memberName = "강우석";
    private static String memberPhone = "01087538643";
    private static MemberGender memberGender = MemberGender.M;

    @BeforeEach
    void be() {
        passwordEncoder = new BCryptPasswordEncoder(); // @SpringBootTest 가 아니므로 Bean 초기화 없음
        password = passwordEncoder.encode("1234");
        memberInfo = new MemberInfo(memberEmail, memberName, memberGender);
    }

    /*
     TODO :: UID, Password Validation 정보에 따른 추가적 Test 필요
     */
    @Test
    @DisplayName("성공 @ 객체 생성함수")
    void 객체_생성함수_성공() {
        // given
        String inputPassword = "1234";
        // when
        Member member = Member.createMember(uid, password, memberInfo);
        // then
        assertThat(member.getUid()).isEqualTo(uid);
        assertThat(passwordEncoder.matches(inputPassword, member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("실패 @ 객체 생성함수")
    void 객체_생성함수_실패() {
        // given
        // when
        // then
        assertThatThrownBy(() -> Member.createMember(null, password, memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember("", password, memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(uid, null, memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(uid, "", memberInfo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.createMember(uid, password, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("성공 @ Refresh Token 변경")
    void RefreshToken_변경_성공() {
        // given
        String changedToken = "NEW_REFRESH_TOKEN";
        Member member = Member.createMember(uid, password, memberInfo);
        // when
        member.changeRefreshToken(changedToken);
        // then
        assertThat(member.getRefreshToken()).isEqualTo(changedToken);
    }

    @Test
    @DisplayName("실패 @ Refresh Token 변경")
    public void RefreshToken_변경_실패() {
        // given
        String changedToken = null;
        Member member = Member.createMember(uid, password, memberInfo);
        // when
        // then
        assertThatThrownBy(() -> member.changeRefreshToken(changedToken)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    public void RefreshToken_fail1() {
        // given
        String changedToken = null;
        Member member = Member.createMember(uid, password, memberInfo);
        // when
        // then
        assertThatThrownBy(() -> member.changeRefreshToken(changedToken)).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    @DisplayName("성공 @ 비밀번호 변경")
    void 비밀번호_변경_성공() {
        // given
        String changedPassword = "CHANGED_PASSWORD";
        Member member = Member.createMember(uid, password, memberInfo);
        // when
        member.changePassword(passwordEncoder.encode(changedPassword));
        String inputPassword = "CHANGED_PASSWORD";
        // then
        assertThat(passwordEncoder.matches(inputPassword, member.getPassword())).isTrue();
    }

    /*
     TODO :: 비밀번호 Validation 정보에 따라 추가적 Test 필요
     */
    @Test
    @DisplayName("실패 @ 비밀번호 변경")
    void 비밀번호_변경_실패() {
        // given
        String wrongPassword = null;
        String wrongPassword2 = "";
        Member member = Member.createMember(uid, password, memberInfo);
        // when
        // then
        assertThatThrownBy(() -> member.changePassword(wrongPassword)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> member.changePassword(wrongPassword2)).isInstanceOf(IllegalArgumentException.class);

    }
}