package com.peoplein.moiming.support;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;

/*
 MOCK - id 를 직접 지정해서 모킹한 모델 (ID Verifying 까지를 위함)
 단위테스트 사용 - Domain
 */
public class TestMockCreator {


    protected AuthTokenReqDto mockTokenReqDto(String refreshToken) {
        AuthTokenReqDto tokenReqDto = new AuthTokenReqDto();
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



    // Category 는 Mocking 할 때 어차피 따로 stubbing 해줘야 함
    protected MoimUpdateReqDto mockMoimUpdateReqDto(Long moimId, String moimName, Integer maxMember, String areaState, String areaCity) {
        return new MoimUpdateReqDto(moimId, moimName, null, maxMember, areaState, areaCity, null);
    }



    protected Member mockMember(Long id, String email, String name, String phone, String ci, Role role) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(password);
        Member mockMember = Member.createMember(
                email, encoded, name, phone, memberGender, memberBirth, fcmToken, ci, role
        );
        mockMember.changeRefreshToken(refreshToken); // 회원가입하면 일단 저장 필요
        mockMember.changeMockObjectIdForTest(id, getClassUrl());
        return mockMember;
    }



    protected Moim mockMoimWithoutRuleJoin(Long id, String mName, int maxMember, String category1, String category2, Member curMember) {

        Category mockCategory1 = mockCategory(1L, CategoryName.fromValue(category1), 1, null);
        Category mockCategory2 = mockCategory(2L, CategoryName.fromValue(category2), 2, mockCategory1);

        Moim moim = Moim.createMoim(mName, moimInfo, maxMember, moimArea, List.of(mockCategory1, mockCategory2), curMember);
        moim.changeMockObjectIdForTest(id, getClassUrl());

        return moim;
    }



    protected Moim mockMoimWithRuleJoin(Long id, String mName, int maxMember, boolean isAgeRule, int maxAge, int minAge, MemberGender memberGender
            , String category1, String category2, Member curMember) {

        Category mockCategory1 = mockCategory(1L, CategoryName.fromValue(category1), 1, null);
        Category mockCategory2 = mockCategory(2L, CategoryName.fromValue(category2), 2, mockCategory1);

        Moim moim = Moim.createMoim(mName, moimInfo, maxMember, moimArea, List.of(mockCategory1, mockCategory2), curMember);
        moim.changeMockObjectIdForTest(id, getClassUrl());
        moim.setMoimJoinRule(mockMoimJoinRule(isAgeRule, maxAge, minAge, memberGender));

        return moim;
    }


    protected MoimMember mockMoimMember(Long id, Member member, Moim moim) {
        MoimMember moimMember = MoimMember.memberJoinMoim(member, moim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        moimMember.changeMockObjectIdForTest(id, getClassUrl());
        return moimMember;
    }



    protected MoimJoinRule mockMoimJoinRule(boolean isAgeRule, int maxAge, int minAge, MemberGender memberGender) {
        MoimJoinRule joinRule = MoimJoinRule.createMoimJoinRule(isAgeRule, maxAge, minAge, memberGender);
        joinRule.changeMockObjectIdForTest(1L, getClassUrl());
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


    // Test Pckg 임을 전달하기 위한 Method
    private URL getClassUrl() {
        return this.getClass().getProtectionDomain().getCodeSource().getLocation();
    }

}