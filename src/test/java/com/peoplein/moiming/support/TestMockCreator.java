package com.peoplein.moiming.support;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.model.dto.request.TokenReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;

/*
 MOCK - id 를 직접 지정해서 모킹한 모델 (ID Verifying 까지를 위함)
 */
public class TestMockCreator {


    protected MemberSignInReqDto mockSigninReqDto() { // 모델들 추가되면 그 때 분할
        return new MemberSignInReqDto(memberEmail, password, memberName, memberPhone, memberGender, notForeigner, memberBirth, fcmToken);
    }

    protected TokenReqDto mockTokenReqDto(String refreshToken) {
        TokenReqDto tokenReqDto = new TokenReqDto();
        tokenReqDto.setGrantType("REFRESH_TOKEN");
        tokenReqDto.setToken(refreshToken);
        return tokenReqDto;
    }

    protected Member mockMember(Long id, String email, String name, String phone, Role role) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(password);
        Member mockMember = Member.createMember(
                email, encoded, name, phone, memberGender, notForeigner, memberBirth, fcmToken, role
        );
        mockMember.changeRefreshToken(refreshToken); // 회원가입하면 일단 저장 필요
        mockMember.changeMockObjectIdForTest(id, this.getClass().getSimpleName());
        return mockMember;
    }


    protected Role mockRole(Long id, RoleType roleType) {
        Role testRole = new Role();
        testRole.setId(id);
        testRole.setRoleDesc("목업");
        testRole.setRoleType(roleType);
        return testRole;
    }


    /*
     Verify Test 를 위한 Test JWT 토큰 생성기
     - generateToken() 함수와 관계성 끊기
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