package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.model.dto.request.MemberInfoReqDto;
import com.peoplein.moiming.service.StorageService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.RoleType.*;
import static com.peoplein.moiming.domain.file.FileDomain.POST;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestParams.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimPostControllerAdvTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member moimCreator, moimMember, notMoimMember, ibwMember, ibfMember, dormantMember, deleteMember;
    private Moim createdMoim;
    private MoimPost creatorPost, moimMemberPost, ibfMemberPost, ibwMemberPost, dormantMemberPost, deleteMemberPost;

    private PostComment deleteParentComment, deleteMemberComment, dormantParentComment, dormantMemberComment;

    @MockBean
    private StorageService storageService; // MEMO :: 전체 로직 내 특정 역할은 진행하지 않도록 한다

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

        // data su 상 moim creator 만 member info 가 있으면 된다
        // 사실 댓글 생성 등도 영향이 있어야 하지만, 인 앱 로직 생성자 입장 상 test 에도 영향을 끼지는게 moim 생성 뿐이라서, 나머지는 무시한다
        moimCreator = makeTestMember(memberPhone, nickname, ci, testRole);
        em.persist(moimCreator);
        MemberInfo curMemberInfo = makeMemberInfo(moimCreator, memberGender, memberBirth);
        em.persist(curMemberInfo);

        moimMember = makeTestMember(memberPhone2, nickname2, ci2, testRole);
        em.persist(moimMember);

        notMoimMember = makeTestMember(memberPhone3, nickname3, ci3, testRole);
        em.persist(notMoimMember);

        ibwMember = makeTestMember(memberPhone4, nickname4, ci4, testRole);
        em.persist(ibwMember);

        ibfMember = makeTestMember(memberPhone5, nickname5, ci5, testRole);
        em.persist(ibfMember);

        dormantMember = makeTestMember(memberPhone6, nickname6, ci6, testRole);
        em.persist(dormantMember);

        deleteMember = makeTestMember(memberPhone7, nickname7, ci7, testRole);
        em.persist(deleteMember);

        // Moim 만들기
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        createdMoim = makeTestMoim(moimName, maxMember, moimArea, List.of(testCategory1, testCategory1_1), moimCreator, -1, -1, MemberGender.N);
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


    void insertTestMemberInfo(Member member, MemberGender memberGender, LocalDate memberBirth) {
        // given - 개인정보 기입완료
        MemberInfo curMemberInfo = makeMemberInfo(member, memberGender, memberBirth);
        em.persist(curMemberInfo);
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
        resultActions.andExpect(jsonPath("$.data.postCategory").value(creatorPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(creatorPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreator.memberId").value(creatorPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreator.nickname").value(creatorPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.postComments").isArray());
        resultActions.andExpect(jsonPath("$.data.postComments", hasSize(3)));

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
        resultActions.andExpect(jsonPath("$.data.postCategory").value(moimMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreator.memberId").value(moimMemberPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreator.nickname").value(moimMemberPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.postComments").isArray());
        resultActions.andExpect(jsonPath("$.data.postComments", hasSize(2)));

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
        resultActions.andExpect(jsonPath("$.data.postCategory").value(creatorPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(creatorPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreator.memberId").value(creatorPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreator.nickname").value(creatorPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.postComments").isArray());
        resultActions.andExpect(jsonPath("$.data.postComments", hasSize(3)));
//        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(3)));
//        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(3)));

        // then - deleted member 가려짐 검증
//        resultActions.andExpect(jsonPath("$.data.childComments." + deleteParentComment.getId() + "[?(@.commentCreatorInfo.memberId == null)]").exists());
//        resultActions.andExpect(jsonPath("$.data.childComments." + deleteParentComment.getId() + "[?(@.commentCreatorInfo.nickname == '탈퇴한 사용자')]").exists());

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
    void getMoimPostDetail_shouldReturn200WithResponse_whenReqIBWPrivatePostByMoimMember() throws Exception {

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
        resultActions.andExpect(jsonPath("$.data.postCategory").value(ibwMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(ibwMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreator.memberId").value(ibwMemberPost.getMember().getId()));
        resultActions.andExpect(jsonPath("$.data.postCreator.nickname").value(ibwMemberPost.getMember().getNickname()));
        resultActions.andExpect(jsonPath("$.data.postComments").isArray());
        resultActions.andExpect(jsonPath("$.data.postComments", hasSize(0)));
//        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(0))); // 조건을 만족하는 것
//        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(0)));

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
        resultActions.andExpect(jsonPath("$.data.postCategory").value(deleteMemberPost.getMoimPostCategory().getValue()));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(deleteMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.postCreator.memberId").isEmpty());
        resultActions.andExpect(jsonPath("$.data.postCreator.nickname").value("탈퇴한 사용자"));
        resultActions.andExpect(jsonPath("$.data.postComments").isArray());
        resultActions.andExpect(jsonPath("$.data.postComments", hasSize(0)));
//        resultActions.andExpect(jsonPath("$.data.parentComments[?(@.depth == 0)]", hasSize(0)));
//        resultActions.andExpect(jsonPath("$.data.childComments", aMapWithSize(0)));

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
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), false, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // given - 요청자 개인 정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

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
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());
        resultActions.andExpect(jsonPath("$.data.updatedAt").exists());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo("새로운 제목");
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(MoimPostCategory.REVIEW);
        assertThat(moimPost.getUpdaterId()).isEqualTo(moimMember.getId());

    }


    // updatePost - 실패 : 개인 정보 미등록 회원의 요청
    @Test
    void updatePost_shouldReturn403_whenNoMemberInfoReq_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), false, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_INFO_NOT_REGISTERED.getErrCode()));

    }


    // updatePost - 성공 : 모두 다 NULL 만 들어왔을 때 - update 해주진 않음
    @Test
    void updatePost_shouldReturn200_whenOnlyNullParamPassed() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), false, null, null, null, null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // given - 요청자 개인 정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
//        resultActions.andExpect(jsonPath("$.code").value(COMMON_UPDATE_REQUEST_FAILED.getErrCode()));


        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo(moimMemberPost.getPostTitle());
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(moimMemberPost.getMoimPostCategory());

    }


    // 2407 - 80 번 테스트와 중복으로 삭제
    // updatePost - 실패 : postId 없음 (Validation)
//    @Test
//    void updatePost_shouldReturn400_whenMoimPostIdNull_byMoimingValidationException() throws Exception {
//
//        // given
//        prepareMoimActivity();
//        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(null, false, null, null, null, null, null, null);
//        String requestBody = om.writeValueAsString(requestDto);
//        String testAccessToken = createTestJwtToken(moimMember, 2000);
//
//        // given - 요청자 개인 정보 등록 완료
//        moimMember = em.find(Member.class, moimMember.getId());
//        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);
//
//        // when
//        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
//                .header(HEADER, PREFIX + testAccessToken)
//                .content(requestBody)
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions.andExpect(status().isBadRequest());
//        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
//        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
//
//    }


    // updatePost - 실패: 일반 모임원의 것을 운영진이 시도
    @Test
    void updatePost_shouldReturn403_whenManagerReqToMoimMemberPost_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), false, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimCreator, 2000);

        // given - 요청자 개인 정보 등록 완료 - data su 시 등록함
//        moimCreator = em.find(Member.class, moimCreator.getId());
//        insertTestMemberInfo(moimCreator, memberGender, memberBirth);

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
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), false, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(notMoimMember, 2000);

        // given - 요청자 개인 정보 등록 완료
        notMoimMember = em.find(Member.class, notMoimMember.getId());
        insertTestMemberInfo(notMoimMember, memberGender3, memberBirth3);

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
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(ibwMemberPost.getId(), false, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(ibwMember, 2000);

        // given - 요청자 개인 정보 등록
        ibwMember = em.find(Member.class, ibwMember.getId());
        insertTestMemberInfo(ibwMember, memberGender4, memberBirth4);

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
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(12345L, false, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_NOT_FOUND.getErrCode()));

    }


    // 240411 Update - fileChanged 필드와 img update 로직 등장 -> 현재 모든 Update 관련 TC 들은 고려되고 있지 않기 때문에, filechanged = false 와 나머지 값들은 null 처리
    //      TODO :: - 파일 수정 관련해서 세부적 테스트 필요 (File 관련이 아니므로 AWS 에 날라갈 요청은 없음)
    // ReqDto Validation Fails (PostId Null, fileChangedNull, PostTitle 5 이하, PostContent 10 이하)
    @Test
    void updatePost_shouldReturn400_whenValidationFails_byMoimingValidationApiException() throws Exception {

        // given
        prepareMoimActivity();
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(null, null, "새로운", "내용", null, null, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(4)));

    }


    // fileChanged = true 인데 hasFiles 가 Null
    @Test
    void updatePost_shouldReturn400_whenFileChangedButHasFileNull_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);

        RelatedFile relatedFile1 = new RelatedFile(0, file1, moimMemberPost.getId(), moimMember.getId());
        RelatedFile relatedFile2 = new RelatedFile(1, file2, moimMemberPost.getId(), moimMember.getId());
        em.persist(relatedFile1);
        em.persist(relatedFile2);

        File newFile1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File newFile2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(newFile1);
        em.persist(newFile2);
        em.flush();
        em.clear();

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given - 요청 시작
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), true, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), null, null, List.of(newFile1.getId(), newFile2.getId()));
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(IMG_STATE_NOT_VALID.getErrCode()));

    }


    // fileChanged = true 인데 List 가 Null - 삭제면 빈리스트 전달
    @Test
    void updatePost_shouldReturn400_whenFileChangedButFileIdsNull_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);

        RelatedFile relatedFile1 = new RelatedFile(0, file1, moimMemberPost.getId(), moimMember.getId());
        RelatedFile relatedFile2 = new RelatedFile(1, file2, moimMemberPost.getId(), moimMember.getId());
        em.persist(relatedFile1);
        em.persist(relatedFile2);

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given - 요청 시작
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), true, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), false, null, null);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(IMG_STATE_NOT_VALID.getErrCode()));

    }


    // fileChanged = true 이며 hasFiles / List 서로 안맞음
    @Test
    void updatePost_shouldReturn400_whenFileChangedButHasFilesAndListStateWrong_byMoimingApiException() throws Exception {

        // given
        prepareMoimActivity();
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);

        RelatedFile relatedFile1 = new RelatedFile(0, file1, moimMemberPost.getId(), moimMember.getId());
        RelatedFile relatedFile2 = new RelatedFile(1, file2, moimMemberPost.getId(), moimMember.getId());
        em.persist(relatedFile1);
        em.persist(relatedFile2);

        File newFile1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File newFile2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(newFile1);
        em.persist(newFile2);
        em.flush();
        em.clear();

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given - 요청 시작
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), true, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), false, null, List.of(newFile1.getId(), newFile2.getId()));
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_POST_UPDATE)
                .header(HEADER, PREFIX + testAccessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(IMG_STATE_NOT_VALID.getErrCode()));

    }


    // fileChanged = true 이며 hasFiles = N / List 가 빈 리스트 - 모든 연관 파일 삭제됨
    @Test
    void updatePost_shouldReturn200_whenFileChangedTrueAndDeleteAllImgs() throws Exception {

        // given data + files
        prepareMoimActivity();
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);

        RelatedFile relatedFile1 = new RelatedFile(0, file1, moimMemberPost.getId(), moimMember.getId());
        RelatedFile relatedFile2 = new RelatedFile(1, file2, moimMemberPost.getId(), moimMember.getId());
        em.persist(relatedFile1);
        em.persist(relatedFile2);

        File newFile1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File newFile2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(newFile1);
        em.persist(newFile2);
        em.flush();
        em.clear();

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), true, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), false, null, new ArrayList<>());
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
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(false)); // 삭제되었으므로
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());
        resultActions.andExpect(jsonPath("$.data.updatedAt").exists());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo("새로운 제목");
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(MoimPostCategory.REVIEW);
        assertThat(moimPost.getUpdaterId()).isEqualTo(moimMember.getId());

        // 성공적으로 삭제됨
        List<RelatedFile> files = em.createQuery("SELECT rf FROM RelatedFile rf WHERE rf.fileDomain = :fileDomain", RelatedFile.class) // 현재 저장된건 테스트 중인 애들 밖에 없으므로
                .setParameter("fileDomain", POST)
                .getResultList();
        assertTrue(files.isEmpty());
    }


    // fileChanged = true 이며 hasFiles = Y / List 가 추가된 것 삭제된 것 존재
    // Design : 1, 2 파일 있었음, 2 삭제되고 nf1, nf2 추가됨, 최종 모습 1, nf1, nf2
    @Test
    void updatePost_shouldReturn200_whenAttemptToAddAndRemovePostImgs() throws Exception {

        // given data + files
        prepareMoimActivity();
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);

        RelatedFile relatedFile1 = new RelatedFile(0, file1, moimMemberPost.getId(), moimMember.getId());
        RelatedFile relatedFile2 = new RelatedFile(1, file2, moimMemberPost.getId(), moimMember.getId());
        em.persist(relatedFile1);
        em.persist(relatedFile2);

        File newFile1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File newFile2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(newFile1);
        em.persist(newFile2);
        em.flush();
        em.clear();

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), true, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), true, null
                , List.of(file1.getId(), newFile1.getId(), newFile2.getId()));
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
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(true));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());
        resultActions.andExpect(jsonPath("$.data.updatedAt").exists());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo("새로운 제목");
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(MoimPostCategory.REVIEW);
        assertThat(moimPost.getUpdaterId()).isEqualTo(moimMember.getId());

        // 성공적으로 삭제됨
        List<RelatedFile> files = em.createQuery("SELECT rf FROM RelatedFile rf " +
                        "WHERE rf.fileDomain = :fileDomain " +
                        "ORDER BY rf.sequence", RelatedFile.class) // 현재 저장된건 테스트 중인 애들 밖에 없으므로
                .setParameter("fileDomain", POST)
                .getResultList();
        assertThat(files.size()).isEqualTo(3);
        assertThat(files.get(0).getFile().getId()).isEqualTo(file1.getId());
        assertThat(files.get(1).getFile().getId()).isEqualTo(newFile1.getId());
        assertThat(files.get(2).getFile().getId()).isEqualTo(newFile2.getId());

    }


    // fileChanged = true 이며 hasFiles = Y / List 가 순서 변경된 것 추가된 것 삭제된 것 존재
    // Design - newFile2, file2, newFile1 으로 변경
    @Test
    void updatePost_shouldReturn200_whenAttemptToAddAndRemoveAndReorderPostImgs() throws Exception {

        // given data + files
        prepareMoimActivity();
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        RelatedFile relatedFile1 = new RelatedFile(0, file1, moimMemberPost.getId(), moimMember.getId());
        RelatedFile relatedFile2 = new RelatedFile(1, file2, moimMemberPost.getId(), moimMember.getId());
        em.persist(relatedFile1);
        em.persist(relatedFile2);

        File newFile1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File newFile2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(newFile1);
        em.persist(newFile2);
        em.flush();
        em.clear();

        // given
        MoimPostUpdateReqDto requestDto = new MoimPostUpdateReqDto(moimMemberPost.getId(), true, "새로운 제목", null, MoimPostCategory.REVIEW.getValue(), true, null
                , List.of(newFile2.getId(), file2.getId(), newFile1.getId()));
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
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(true));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(moimMemberPost.getCommentCnt()));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());
        resultActions.andExpect(jsonPath("$.data.updatedAt").exists());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

        // then - db verify
        em.flush();
        em.clear();

        MoimPost moimPost = em.find(MoimPost.class, moimMemberPost.getId());
        assertThat(moimPost.getPostTitle()).isEqualTo("새로운 제목");
        assertThat(moimPost.getPostContent()).isEqualTo(moimMemberPost.getPostContent());
        assertThat(moimPost.getMoimPostCategory()).isEqualTo(MoimPostCategory.REVIEW);
        assertThat(moimPost.getUpdaterId()).isEqualTo(moimMember.getId());

        // 성공적으로 삭제됨
        List<RelatedFile> files = em.createQuery("SELECT rf FROM RelatedFile rf " +
                        "WHERE rf.fileDomain = :fileDomain " +
                        "ORDER BY rf.sequence", RelatedFile.class) // 현재 저장된건 테스트 중인 애들 밖에 없으므로
                .setParameter("fileDomain", POST)
                .getResultList();
        assertThat(files.size()).isEqualTo(3);
        assertThat(files.get(0).getFile().getId()).isEqualTo(newFile2.getId());
        assertThat(files.get(1).getFile().getId()).isEqualTo(file2.getId());
        assertThat(files.get(2).getFile().getId()).isEqualTo(newFile1.getId());

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

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

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


    // deletePost - 실패 : 개인 정보 기입 안된 사용자
    @Test
    void deletePost_shouldReturn403_whenNoMemberInfoReq_byMoimingApiException() throws Exception {

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
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_INFO_NOT_REGISTERED.getErrCode()));

    }


    // deletePost - 성공: 일반 모임원의 것을 운영진이 시도
    @Test
    void deletePost_shouldReturn200_whenManagerReqToDeleteOtherMoimMemberPost() throws Exception {

        // given
        prepareMoimActivity();
        String[] beforeParams = {"moimId", "moimPostId"};
        String[] afterParams = {createdMoim.getId() + "", moimMemberPost.getId() + ""};
        String testAccessToken = createTestJwtToken(moimCreator, 2000);

        // given - 개인정보 등록 완료 - data su 에서 진행하는걸로 변경
//        moimCreator = em.find(Member.class, moimCreator.getId());
//        insertTestMemberInfo(moimCreator, memberGender, memberBirth);

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

        // given - 개인정보 등록 완료
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

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

        // given - 요청자 개인 정보 입력
        notMoimMember = em.find(Member.class, notMoimMember.getId());
        insertTestMemberInfo(notMoimMember, memberGender3, memberBirth3);

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

        // given - 요청자 개인 정보 입력
        ibfMember = em.find(Member.class, ibfMember.getId());
        insertTestMemberInfo(ibfMember, memberGender5, memberBirth5);

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

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_POST_DELETE, beforeParams, afterParams))
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_NOT_FOUND.getErrCode()));

    }

}

