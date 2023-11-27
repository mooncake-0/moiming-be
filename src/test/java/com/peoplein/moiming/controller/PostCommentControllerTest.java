package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.support.TestModelParams.moimArea;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PostCommentControllerTest extends TestObjectCreator {

    public final ObjectMapper om = new ObjectMapper();
    @Autowired
    private MockMvc mvc;
    @Autowired
    private EntityManager em;
    private Member moimCreator, moimMember, notMoimMember, inactiveMember;
    private Moim testMoim;
    private MoimPost testMoimPost;


    // Post Comment 생성 요청 - Normal Comment
    @Test
    void createComment_shouldReturn200_whenNormalCommentByMoimMember() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null, 0);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        // 값은 Repo Integrated Test 로 확인완료
    }


    // Post Comment 생성 요청 - Reply Comment
    @Test
    void createComment_shouldReturn200_whenReplyCommentByMoimMember() throws Exception {

        // given
        PostComment parentComment = makePostComment(moimCreator, testMoimPost, 0, null);
        em.persist(parentComment);
        em.flush();
        em.clear();

        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), parentComment.getId(), 1);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        // 값은 Repo Integrated Test 로 확인완료
    }


    // Validation 요청 오류 점검 1) moimId null 2) postId null 3) depth null
    @Test
    void createComment_shouldReturn400_whenRequestValidationFails_byMoimingValidationException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(null, null, null, 0);
        requestDto.setDepth(null);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(3)));

    }


    // Validation 요청 오류 점검 1) content over 100
    @Test
    void createComment_shouldReturn400_whenRequestValidationFailsByContentLength_byMoimingValidationException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null, 0);
        String contentOver100 = "a" + "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        requestDto.setContent(contentOver100);

        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

    }


    // Post Comment 생성 요청 오류 - Not Moim Member
    @Test
    void createComment_shouldReturn400_whenNotMoimMemberRequest_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null, 0);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(notMoimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));
    }


    // Post Comment 생성 요청 오류 - Moim Member Not Active
    @Test
    void createComment_shouldReturn400_whenMoimMemberNotActiveRequest_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null, 0);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(inactiveMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_ACTIVE.getErrCode()));
    }


    // Post Comment 생성 요청 오류 - Parent Comment Not Found
    @Test
    void createComment_shouldReturn400_whenParentCommentNotFound_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), 1234L, 1);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_COMMENT_NOT_FOUND.getErrCode()));
    }


    // Post Comment 생성 요청 오류 - Parent Comment Mapping Error 하나만
    @Test
    void createComment_shouldReturn400_whenParentCommentMappingWrong_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null , 1);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_PARAM.getErrCode()));

    }



    @BeforeEach
    void su() {

        // Member moimMember, moimCreator
        Role testRole = makeTestRole(RoleType.USER);
        moimCreator =makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        notMoimMember = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        inactiveMember = makeTestMember(memberEmail4, memberPhone4, memberName4, nickname4, ci4, testRole);
        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(notMoimMember);
        em.persist(inactiveMember);

        // Moim moim 존재
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);


        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember.memberJoinMoim(inactiveMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        em.persist(testMoim);

        // Post post 존재
        testMoimPost = makeMoimPost(testMoim, moimCreator, MoimPostCategory.NOTICE, false);
        em.persist(testMoimPost);

        em.flush();
        em.clear();

    }
}