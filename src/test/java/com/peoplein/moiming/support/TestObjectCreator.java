package com.peoplein.moiming.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;
import static com.peoplein.moiming.support.TestDto.*;
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


    // 카테고리가 저장된 상태여야 하기 떄문에 그냥 저장 후 값을 전달
    protected Moim makeTestMoim(String name, int mMember, String state, String city, List<Category> categories, Member curMember) {
        return Moim.createMoim(name, moimInfo, mMember, new Area(state, city), categories, curMember);
    }




    /*
     DTO Creator
     */
    protected TestMemberRequestDto makeMemberReqDto(String email, String name, String phone) {
        return new TestMemberRequestDto(email, password, name, phone, memberGender, notForeigner, memberBirthStringFormat, fcmToken);
    }

    protected MoimCreateReqDto makeMoimReqDtoNoJoinRule(String mName, String state, String city, int mMember, String category1, String category2) {
        return new MoimCreateReqDto(
                mName, moimInfo, state, city, mMember, false, null, List.of(category1, category2)
        );
    }

    protected MoimCreateReqDto makeMoimReqDtoWithJoinRule(String mName, String state, String city, int mMember, String category1, String category2) {
        JoinRuleCreateReqDto joinRuleCreateDto = new JoinRuleCreateReqDto(true, 40, 20, MemberGender.M);
        return new MoimCreateReqDto(
                mName, moimInfo, state, city, mMember, true, joinRuleCreateDto, List.of(category1, category2)
        );
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