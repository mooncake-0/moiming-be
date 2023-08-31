package com.peoplein.moiming.support;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.model.dto.request.TokenReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;
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


    protected MoimCreateReqDto mockMoimCreateReqDto(String mName, int maxMember, boolean hasJoinRule
            , boolean isAgeRule, int minAge, int maxAge, MemberGender memberGender, String category1, String category2) {


        JoinRuleCreateReqDto ruleRequestDto = null;
        if (hasJoinRule) ruleRequestDto = new JoinRuleCreateReqDto(isAgeRule, maxAge, minAge, memberGender);
        return new MoimCreateReqDto(mName, moimInfo, moimArea.getCity(), moimArea.getState()
                , maxMember, hasJoinRule, ruleRequestDto, List.of(category1, category2));

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


    protected Moim mockMoim(Long id, String mName, int maxMember, boolean hasRuleJoin
            , boolean isAgeRule, int maxAge, int minAge, MemberGender memberGender, String category1, String category2, Member curMember) {

        Category mockCategory1 = mockCategory(1L, CategoryName.fromValue(category1), 1, null);
        Category mockCategory2 = mockCategory(2L, CategoryName.fromValue(category2), 2, mockCategory1);

        Moim moim = Moim.createMoim(mName, moimInfo, maxMember, moimArea, List.of(mockCategory1, mockCategory2), curMember);
        moim.changeMockObjectIdForTest(id, getClass());

        if (hasRuleJoin) {
            moim.setMoimJoinRule(mockMoimJoinRule(isAgeRule, maxAge, minAge, memberGender));
        }


        return moim;
    }


    protected MoimJoinRule mockMoimJoinRule(boolean isAgeRule, int maxAge, int minAge, MemberGender memberGender) {
        MoimJoinRule joinRule = MoimJoinRule.createMoimJoinRule(isAgeRule, maxAge, minAge, memberGender);
        joinRule.changeMockObjectIdForTest(1L, getClass().getName());
        return joinRule;
    }

    protected Role mockRole(Long id, RoleType roleType) {
        Role testRole = new Role();
        testRole.setId(id);
        testRole.setRoleDesc("목업");
        testRole.setRoleType(roleType);
        return testRole;
    }


    protected Category mockCategory(Long id, CategoryName categoryName, int depth, Category parent) {
        return new Category(id, categoryName, depth, parent);
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