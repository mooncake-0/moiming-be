package com.peoplein.moiming.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.security.token.JwtParams;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static com.peoplein.moiming.support.TestModelParams.*;

public class TestObjectCreator {

    protected Member makeTestMember(String email, String phone, String name, Role role) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(password);
        return Member.createMember(email, encoded, name, phone, memberGender, notForeigner, memberBirth, fcmToken, role);
    }

    protected Role makeTestRole(RoleType roleType) {
        return new Role(1L, "일반유저", roleType);
    }

    /*
    MockCreator 와 동일하게 Custom Jwt 토큰을 생성해볼 수 있다
    */
    protected String createTestJwtToken(Member testMember, int expiresPlus) {

        long expiresAt = System.currentTimeMillis() + expiresPlus;

        return JWT.create()
                .withSubject(JwtParams.TEST_JWT_SUBJECT)
                .withExpiresAt(new Date(expiresAt))
                .withClaim(JwtParams.CLAIM_KEY_MEMBER_EMAIL, testMember.getMemberEmail())
                .sign(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET));
    }


}