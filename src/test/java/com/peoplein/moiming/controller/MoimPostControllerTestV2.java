package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.RoleType.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// MEMO :: 단일 Test 확인시 기본적으로 String responseBody 를 출력하고, json 에 붙여넣어서 확인한다
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimPostControllerTestV2 extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member moimCreator, moimMember, notMoimMember, ibwMember, ibfMember, dormantMember, deleteMember;
    private Moim createdMoim;
    private MoimPost creatorPost, moimMemberPost, ibfMemberPost, ibwMemberPost, dormantMemberPost, deleteMemberPost;

    private PostComment deleteParentComment, deleteMemberComment, dormantParentComment, dormantMemberComment;

    // 특정 모임을 만든다
    // 모임원 2명 (운영자 포함) , 비모임원 1 명, 강퇴 인원 1명, 나간 인원 1명, 휴면 인원 1명, 계정 삭제 인원 1명
    // 게시물 6개
    // 운영자 1개 (공지), 일반 모임원 1개,  강퇴 인원 1개, 나간 인원 1 개, 휴면 1개, 삭제 인원 1 개, --> 다 남기고 처리된 것
    // 운영자 게시물 -> 댓글 6개
    // 일반 모임원1개 & 운영자 답글 1개  //  강퇴 인원 1개 & 운영자 답글 1 개 //  나간 인원 1개  & 모임원 답글 1개 &  삭제 인원 답글 1개
    // 일반 모임원 게시물 -> 댓글 4개
    // 운영자 1개 & 모임원 답글 1개 & 휴면 인원 답글 1개 // 강퇴 인원 1 개 댓글
    void prepareMoimActivity() {

        // Member 만들기
        Role testRole = makeTestRole(USER);
        em.persist(testRole);

        moimCreator = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        notMoimMember = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        ibwMember = makeTestMember(memberEmail4, memberPhone4, memberName4, nickname4, ci4, testRole);
        ibfMember = makeTestMember(memberEmail5, memberPhone5, memberName5, nickname5, ci5, testRole);
        dormantMember = makeTestMember(memberEmail6, memberPhone6, memberName6, nickname6, ci6, testRole);
        deleteMember = makeTestMember(memberEmail7, memberPhone7, memberName7, nickname7, ci7, testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(notMoimMember);
        em.persist(ibwMember);
        em.persist(ibfMember);
        em.persist(dormantMember);
        em.persist(deleteMember);

        // Moim 만들기
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        createdMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        em.persist(createdMoim);

        // MoimMember 만들기
        MoimMember.memberJoinMoim(moimMember, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember.memberJoinMoim(ibwMember, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.IBW);
        MoimMember.memberJoinMoim(ibfMember, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        MoimMember.memberJoinMoim(dormantMember, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.IBD);
        MoimMember.memberJoinMoim(deleteMember, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.NOTFOUND);

        // MoimPost 만들기
        creatorPost = makeMoimPost(createdMoim, moimCreator, MoimPostCategory.NOTICE, false);
        moimMemberPost = makeMoimPost(createdMoim, moimMember, MoimPostCategory.GREETING, true);
        ibfMemberPost = makeMoimPost(createdMoim, ibfMember, MoimPostCategory.GREETING, false);
        ibwMemberPost = makeMoimPost(createdMoim, ibwMember, MoimPostCategory.EXTRA, true);
        dormantMemberPost = makeMoimPost(createdMoim, dormantMember, MoimPostCategory.REVIEW, false);
        deleteMemberPost = makeMoimPost(createdMoim, deleteMember, MoimPostCategory.EXTRA, false);
        em.persist(creatorPost);
        em.persist(moimMemberPost);
        em.persist(ibfMemberPost);
        em.persist(ibwMemberPost);
        em.persist(dormantMemberPost);
        em.persist(deleteMemberPost);

        // 댓글들
        PostComment comment1_1 = makePostComment(moimMember, creatorPost, 0, null);
        PostComment comment1_2 = makePostComment(moimCreator, creatorPost, 1, comment1_1);
        PostComment comment1_3 = makePostComment(ibfMember, creatorPost, 0, null);
        PostComment comment1_4 = makePostComment(moimCreator, creatorPost, 1, comment1_3);
        deleteParentComment = makePostComment(ibwMember, creatorPost, 0, null);
        PostComment comment1_6 = makePostComment(moimMember, creatorPost, 1, deleteParentComment);
        deleteMemberComment = makePostComment(deleteMember, creatorPost, 1, deleteParentComment);

        dormantParentComment = makePostComment(moimCreator, moimMemberPost, 0, null);
        PostComment comment2_2 = makePostComment(moimMember, moimMemberPost, 1, dormantParentComment);
        dormantMemberComment = makePostComment(dormantMember, moimMemberPost, 1, dormantParentComment);
        PostComment comment2_4 = makePostComment(ibfMember, moimMemberPost, 0, null);

        em.flush();
        em.clear();

    }


    // 성공 :: MoimMember 가 조회를 요청
    @Test
    void getMoimPostDetail_shouldReturn200WithResponse_whenReqPublicPostByMoimMember() throws Exception {
        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", creatorPost.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(creatorPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(creatorPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(creatorPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.memberId").value(creatorPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.nickname").value(creatorPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.parentComments").isArray());
        resultActions.andExpect(jsonPath("$.data.parentComments", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(3)));
        // then - deleted member 가려짐 검증
        resultActions.andExpect(jsonPath("$.data.childComments." + deleteParentComment.getId() + "[?(@.commentCreatorInfo.memberId == null)]").exists());
        resultActions.andExpect(jsonPath("$.data.childComments." + deleteParentComment.getId() + "[?(@.commentCreatorInfo.nickname == '탈퇴한 사용자')]").exists());

    }


    // 성공 :: MoimMember 가 private 한 게시물을 요청
    @Test
    void getMoimPostDetail_shouldReturn200WithResponse_whenReqPrivatePostByMoimMember() throws Exception {

        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", moimMemberPost.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(moimMemberPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(moimMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.memberId").value(moimMemberPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.nickname").value(moimMemberPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.parentComments").isArray());
        resultActions.andExpect(jsonPath("$.data.parentComments", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(2)));
        // then - deleted member 가려짐 검증
        resultActions.andExpect(jsonPath("$.data.childComments." + dormantParentComment.getId() + "[?(@.commentCreatorInfo.memberId == null)]").exists());
        resultActions.andExpect(jsonPath("$.data.childComments." + dormantParentComment.getId() + "[?(@.commentCreatorInfo.nickname == '휴면 전환 사용자')]").exists());

    }


    // 성공 :: IBF 가 공개된 게시물 요청
    @Test
    void getMoimPostDetail_shouldReturn200WithResponse_whenReqPublicPostByIBFMoimMember() throws Exception {

        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(ibfMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", creatorPost.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(creatorPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(creatorPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(creatorPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.memberId").value(creatorPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.nickname").value(creatorPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.parentComments").isArray());
        resultActions.andExpect(jsonPath("$.data.parentComments", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(3)));

        // then - deleted member 가려짐 검증
        resultActions.andExpect(jsonPath("$.data.childComments." + deleteParentComment.getId() + "[?(@.commentCreatorInfo.memberId == null)]").exists());
        resultActions.andExpect(jsonPath("$.data.childComments." + deleteParentComment.getId() + "[?(@.commentCreatorInfo.nickname == '탈퇴한 사용자')]").exists());

    }


    // 실패 :: IBF 가 private 한 게시물 요청
    @Test
    void getMoimPostDetail_shouldReturn403WithResponse_whenReqPrivatePostByIBFMoimMember_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(ibfMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", moimMemberPost.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_ACT_NOT_AUTHORIZED.getErrCode()));

    }


    // 성공 :: 나간 유저의 게시물 요청
    @Test
    void getMoimPostDetail_shouldReturn200WithResponse_whenReqIBWPrivatePostByMoimMember_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", ibwMemberPost.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(ibwMemberPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(ibwMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(ibwMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.memberId").value(ibwMemberPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.nickname").value(ibwMemberPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.parentComments").isArray());
        resultActions.andExpect(jsonPath("$.data.parentComments", hasSize(0)));
        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(0))); // 조건을 만족하는 것
        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(0)));

    }


    // 성공 :: Deleted 유저의 게시물 요청
    @Test
    void getMoimPostDetail_shouldReturn200WithResponse_whenReqDeletePublicPostByMoimMember_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", deleteMemberPost.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(deleteMemberPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(deleteMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(deleteMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.memberId").isEmpty());
        resultActions.andExpect(jsonPath("$.data.postCreatorInfo.nickname").value("탈퇴한 사용자"));
        resultActions.andExpect(jsonPath("$.data.parentComments").isArray());
        resultActions.andExpect(jsonPath("$.data.parentComments", hasSize(0)));
        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(0)));
        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(0)));

    }


    // 실패 :: 없는 게시물을 요청
    @Test
    void getMoimPostDetail_shouldReturn404WithResponse_whenReqNotExistingPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String testAccessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", 12345L + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_DETAIL, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken));

        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_NOT_FOUND.getErrCode()));
    }


    // 수정 VERIFY 필요
    // updatePost - 성공 : 원하는 것만 들어 있을 때 성공
    @Test
    void updatePost_shouldReturn200_whenOnlyNeededParamPassed() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.moimPostId").value(moimMemberPost.getId()));
        resultActions.andExpect(jsonPath("$.data.postTitle").value("새로운 제목"));
        resultActions.andExpect(jsonPath("$.data.postContent").value(moimMemberPost.getPostContent()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(MoimPostCategory.REVIEW.getValue()));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(moimMemberPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(moimMemberPost.isHasFiles()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.createdAt").value(moimMemberPost.getCreatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo("새로운 제목");
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(MoimPostCategory.REVIEW);
        assertThat(moimPost.getUpdatedMemberId()).isEqualTo(moimMember.getId());

    }


    // updatePost - 성공 : 모두 다 NULL 만 들어왔을 때 - update 해주진 않음
    @Test
    void updatePost_shouldReturn200_whenOnlyNullParamPassed() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), null, null, null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.moimPostId").value(moimMemberPost.getId()));
        resultActions.andExpect(jsonPath("$.data.postTitle").value(moimMemberPost.getPostTitle()));
        resultActions.andExpect(jsonPath("$.data.postContent").value(moimMemberPost.getPostContent()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(moimMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(moimMemberPost.isHasPrivateVisibility()));
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(moimMemberPost.isHasFiles()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.createdAt").value(moimMemberPost.getCreatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo(moimMemberPost.getPostTitle());
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(moimMemberPost.getMoimPostCategory());
        assertThat(moimPost.getUpdatedMemberId()).isEqualTo(moimMember.getId());

    }


    // updatePost - 실패 : postId 없음 (Validation)
    @Test
    void updatePost_shouldReturn400_whenMoimPostIdNull_byMoimingValidationException() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(null, null, null, null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // updatePost - 실패: 일반 모임원의 것을 운영진이 시도
    @Test
    void updatePost_shouldReturn403_whenManagerReqToMoimMemberPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimCreator, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));

    }


    // updatePost - 실패 : 비 모임원의 시도
    @Test
    void updatePost_shouldReturn404_whenNotMoimMemberReqToMoimMemberPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(notMoimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

    }


    // updatePost - 실패 : IBW 의 예전 자기 게시물 수정 시도
    @Test
    void updatePost_shouldReturn403_whenIBWReqToOwnIBWMoimPost_byMoimingApiException() throws Exception {
        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(ibwMemberPost.getId(), "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(ibwMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));
    }


    // updatePost - 실패 : 게시물 없음
    @Test
    void updatePost_shouldReturn404_whenReqToNotExistingPost_byMoimingApiException() throws Exception {
        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(12345L, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_NOT_FOUND.getErrCode()));

    }


    // 삭제 VERIFY 필요
    // deletePost - 성공: 일반 모임원이 자기 것을 시도
    @Test
    void deletePost_shouldReturn200_whenMoimMemberReqToDeleteOwnPost() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", moimMemberPost.getId() + ""};
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertNull(moimPost);
        List<PostComment> postComments = em.createQuery("select pc from PostComment pc where pc.moimPost.id = :moimPostId", PostComment.class)
                .setParameter("moimPostId", moimMemberPost.getId())
                .getResultList();
        assertTrue(postComments.isEmpty());

    }


    // deletePost - 성공: 일반 모임원의 것을 운영진이 시도
    @Test
    void deletePost_shouldReturn200_whenManagerReqToDeleteOtherMoimMemberPost() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", moimMemberPost.getId() + ""};
        String testAccessToken = createTestJwtToken(moimCreator, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertNull(moimPost);
        List<PostComment> postComments = em.createQuery("select pc from PostComment pc where pc.moimPost.id = :moimPostId", PostComment.class)
                .setParameter("moimPostId", moimMemberPost.getId())
                .getResultList();
        assertTrue(postComments.isEmpty());

    }


    // deletePost - 실패: 운영진(타 모임원)의 것을 일반 모임원이 시도
    @Test
    void deletePost_shouldReturn403_whenNormalReqToDeleteOthersPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", creatorPost.getId() + ""};
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));

        // then - db verify
        MoimPost moimPost = em.find(MoimPost.class, creatorPost.getId());
        assertNotNull(moimPost);

    }


    // deletePost - 실패 : 비 모임원의 시도
    @Test
    void deletePost_shouldReturn404_whenNotMoimMemberReqToDeleteMoimMemberPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", moimMemberPost.getId() + ""};
        String testAccessToken = createTestJwtToken(notMoimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

        // then - db verify
        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertNotNull(moimPost);

    }


    // deletePost - 실패 : IBF 의 예전 자기 게시물 삭제 시도
    @Test
    void deletePost_shouldReturn403_whenIBFMemberReqToDeleteIBFOwnPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", ibfMemberPost.getId() + ""};
        String testAccessToken = createTestJwtToken(ibfMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));

        // then - db verify
        MoimPost moimPost = em.find(MoimPost.class, ibfMemberPost.getId());
        assertNotNull(moimPost);

    }


    // deletePost - 실패 : 게시물 없음
    @Test
    void deletePost_shouldReturn404_whenMoimMemberReqToDeleteNotExistingPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", 12345L + ""};
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_NOT_FOUND.getErrCode()));

    }

}

