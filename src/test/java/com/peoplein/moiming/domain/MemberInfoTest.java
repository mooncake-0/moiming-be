package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.exception.BadAuthParameterInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public class MemberInfoTest {

    /*
     예상 : 초기 필수 입력 정보
     */
    private static String memberEmail = "a@moiming.net";
    private static String memberName = "강우석";
    private static String memberPhone = "01087538643";
    private static MemberGender memberGender = MemberGender.M;

    /*
     추가 입력 정보
     */
    private static LocalDate memberBirth = LocalDate.of(1995, 12, 18);
    private static String memberPfImg = "/user/pf-img/wrock.kang.jpg";
    private static boolean isDormant = false;

    /*
     TODO :: NICE 연동후 생성 Test 생성자 변수 추가
    */
    @Test
    @DisplayName("성공 @ 객체 생성자")
    void 객체_생성자_성공() {
        // given
        // when
        MemberInfo memberInfo = new MemberInfo(memberEmail, memberName, memberGender);

        // then
        assertEquals(memberEmail, memberInfo.getMemberEmail());
        assertEquals(memberName, memberInfo.getMemberName());
        assertEquals(memberGender, memberInfo.getMemberGender());
    }

    /*
     TODO :: Validation 정보에 따른 추가적 Test 필요
     */
    @Test
    @DisplayName("실패 @ 객체 생성함수")
    void 객체_생성자_실패() {
        //given
        //when
        //then
        assertThatThrownBy(() -> new MemberInfo("", memberName, memberGender)).isInstanceOf(BadAuthParameterInputException.class);
        assertThatThrownBy(() -> new MemberInfo(null, memberName, memberGender)).isInstanceOf(BadAuthParameterInputException.class);
        assertThatThrownBy(() -> new MemberInfo(memberEmail, "", memberGender)).isInstanceOf(BadAuthParameterInputException.class);
        assertThatThrownBy(() -> new MemberInfo(memberEmail, null, memberGender)).isInstanceOf(BadAuthParameterInputException.class);
        assertThatThrownBy(() -> new MemberInfo(memberEmail, memberName, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
