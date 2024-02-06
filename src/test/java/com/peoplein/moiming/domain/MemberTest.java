package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MemberTest extends TestObjectCreator {


    @Test
    void getMaskedEmail_shouldReturnMaskedEmail_whenLength1() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        String emailLength1 = "a@moiming.io";
        Member member = makeTestMember(emailLength1, memberPhone, memberName, nickname, ci, testRole);

        // when
        String maskedEmail = member.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("*@moiming.io");

    }


    @Test
    void getMaskedEmail_shouldReturnMaskedEmail_whenLength2() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        String emailLength2 = "ab@moiming.io";
        Member member = makeTestMember(emailLength2, memberPhone, memberName, nickname, ci, testRole);

        // when
        String maskedEmail = member.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("**@moiming.io");

    }


    @Test
    void getMaskedEmail_shouldReturnMaskedEmail_whenLength3() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        String emailLength3 = "abc@moiming.io";
        Member member = makeTestMember(emailLength3, memberPhone, memberName, nickname, ci, testRole);

        // when
        String maskedEmail = member.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("a**@moiming.io");

    }


    @Test
    void getMaskedEmail_shouldReturnMaskedEmail_whenLength5() {

        // given
        Role testRole = makeTestRole(RoleType.USER);
        String emailLength3 = "abcde@moiming.io";
        Member member = makeTestMember(emailLength3, memberPhone, memberName, nickname, ci, testRole);

        // when
        String maskedEmail = member.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("ab***@moiming.io");

    }

}
