package com.peoplein.moiming.service.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberNicknameCreatorTest {

    /*
     전부 Composition 관계, mocking 할 것 없음
     */
    MemberNicknameCreator nicknameCreator = new MemberNicknameCreator();

    @Test
    void createNickname_should_return_created_nickname() {

        // given
        // when
        String nickname1 = nicknameCreator.createNickname();
        String nickname2 = nicknameCreator.createNickname();
        String nickname3 = nicknameCreator.createNickname();


        // then
        assertTrue(StringUtils.hasText(nickname1));
        assertTrue(StringUtils.hasText(nickname2));
        assertTrue(StringUtils.hasText(nickname3));

        System.out.println(nickname1);
        System.out.println(nickname2);
        System.out.println(nickname3);

    }
}

