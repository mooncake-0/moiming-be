package com.peoplein.moiming.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto;
import com.peoplein.moiming.model.dto.request.PostCommentReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.service.util.MemberNicknameCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityManager;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.MemberSignInReqDto.*;
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

        Member testMember = Member.createMember(email, encoded, name, phone, memberGender, notForeigner, memberBirth, fcmToken, ci, role);
        testMember.changeNickname(nickname);

        return testMember;
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


    protected TestMemberRequestDto makeMemberReqDto(String email, String name, String phone, String ci, List<PolicyAgreeDto> policyDtos) {
        return new TestMemberRequestDto(email, password, name, phone, memberGender, notForeigner, memberBirthStringFormat, fcmToken, ci, policyDtos);
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


    // 일일이 다 반환해서 또 em 돌리기 귀찮으니, 그냥 받아서 사용한다
    // 스레드 공유가 아니므로 괜찮음. 단일 스레드에서 함수로 역할만 분리한거
    protected void makeMoimPosts(int postCnt, Moim moim, Member member, EntityManager em) throws InterruptedException {

        MoimPostCategory category = null;
        boolean hasPrivateVisibility = false;

        Random random = new Random();

        for (int i = 0; i < postCnt; i++) {

            int categoryRandom = random.nextInt(4);
            int visibilityRandom = random.nextInt(2);

            if (categoryRandom % 4 == 0) category = MoimPostCategory.NOTICE;
            if (categoryRandom % 4 == 1) category = MoimPostCategory.GREETING;
            if (categoryRandom % 4 == 2) category = MoimPostCategory.REVIEW;
            if (categoryRandom % 4 == 3) category = MoimPostCategory.EXTRA;
            if (visibilityRandom % 2 == 0) hasPrivateVisibility = true;
            if (visibilityRandom % 2 == 1) hasPrivateVisibility = false;

            String title = "제목" + visibilityRandom + i + categoryRandom;
            MoimPost post = MoimPost.createMoimPost(title, "내용", category, hasPrivateVisibility, false, moim, member);
            if (i % 3 == 1) {
                Thread.sleep(100);
            }
            em.persist(post);
        }

        em.flush();
        em.clear();
    }

}