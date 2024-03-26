package com.peoplein.moiming.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.security.token.JwtParams;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignInReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;
import static com.peoplein.moiming.support.TestDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;

public class TestObjectCreator {

    protected Member makeTestMember(String email, String phone, String name, String nickname, String ci, Role role) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(password);

        Member testMember = Member.createMember(email, encoded, name, phone, memberGender, memberBirth, fcmToken, ci, role);
        testMember.changeNickname(nickname);

        return testMember;
    }

    protected SmsVerification makeTestSmsVerification(boolean verified, Long memberId, String memberPhoneNumber, VerificationType type) {
        SmsVerification verification = SmsVerification.createSmsVerification(memberId, memberPhoneNumber, type);
        if (verified) verification.confirmVerification(type, verification.getVerificationNumber());
        return verification;
    }


    protected Role makeTestRole(RoleType roleType) {
        return new Role(1L, "일반유저", roleType);
    }


    // 카테고리가 저장된 상태여야 하기 떄문에 그냥 저장 후 값을 전달
    protected Moim makeTestMoim(String name, int mMember, String state, String city, List<Category> categories, Member curMember) {
        return Moim.createMoim(name, moimInfo, mMember, new Area(state, city), categories, curMember);
    }


    protected MoimJoinRule makeTestMoimJoinRule(boolean flag, int max, int min, MemberGender gender) {
        return MoimJoinRule.createMoimJoinRule(flag, max, min, gender);
    }


    /*
     DTO Creator
     */

    protected List<PolicyAgreeDto> makePolicyReqDtoList(boolean[] hasAgreeds, PolicyType[] types) {
        List<PolicyAgreeDto> policyDtos = new ArrayList<>();

        for (int i = 0; i < types.length; i++) {
            policyDtos.add(new PolicyAgreeDto(hasAgreeds[i], types[i]));
        }

        return policyDtos;
    }


    protected List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> makePolicyUpdateReqDtoList(Boolean[] hasAgreeds, PolicyType[] types) {
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> policyDtos = new ArrayList<>();

        for (int i = 0; i < types.length; i++) {
            policyDtos.add(new PolicyAgreeUpdateReqDto.PolicyAgreeDto(hasAgreeds[i], types[i]));
        }

        return policyDtos;
    }


    protected MoimCreateReqDto makeMoimReqDtoNoJoinRule(String mName, String state, String city, int mMember, String category1, String category2) {
        return new MoimCreateReqDto(
                mName, moimInfo, state, city, mMember, false, null, null, List.of(category1, category2)
        );
    }

    protected MoimCreateReqDto makeMoimReqDtoWithJoinRule(String mName, String state, String city, int mMember, String category1, String category2) {
        JoinRuleCreateReqDto joinRuleCreateDto = new JoinRuleCreateReqDto(true, 40, 20, MemberGender.M);
        return new MoimCreateReqDto(
                mName, moimInfo, state, city, mMember, true, joinRuleCreateDto, null, List.of(category1, category2)
        );
    }


    // moimInfo, moimAreaCity 는 변경하지 않는다고 가정
    protected MoimUpdateReqDto makeMoimUpdateReqDto(Long moimId, String mName, Integer mMember, String state, String category1, String category2) {
        return new MoimUpdateReqDto(moimId, mName, null, mMember, state, null, List.of(category1, category2));
    }


    protected PostCommentCreateReqDto makeCommentCreateReqDto(Long postId, Long parentId, Integer depth) {
        String type = depth == 0 ? "댓글입니다" : "답글입니다";
        String content = postId + ", " + type;
        return new PostCommentCreateReqDto(postId, parentId, content, depth);
    }


    protected PostCommentUpdateReqDto makeCommentUpdateReqDto(Long commentId) {
        String changedContent = "수정된 댓글 내용입니다";
        return new PostCommentUpdateReqDto(commentId, changedContent);
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


    protected MoimPost makeMoimPost(Moim moim, Member member, MoimPostCategory category, boolean hasPrivateVisibility) {
        return MoimPost.createMoimPost("TITLE", "CONTENT", category, hasPrivateVisibility, false, moim, member);
    }


    protected PostComment makePostComment(Member member, MoimPost moimPost, int depth, PostComment parent) {
        String type = depth == 0 ? "댓글입니다" : "답글입니다";
        String content = moimPost.getPostTitle() + ", " + member.getNickname() + ", " + type;
        return PostComment.createPostComment(content, member, moimPost, depth, parent);
    }

}