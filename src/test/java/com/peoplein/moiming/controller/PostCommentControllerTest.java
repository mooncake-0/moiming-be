package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.member.Member;
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
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private Member moimCreator, moimMember, moimMember2, notMoimMember, inactiveMember;
    private Moim testMoim;
    private MoimPost testMoimPost;
    private PostComment prePostComment;
    private String changedContent = "수정된 내용입니다";


    // Post Comment 생성 요청 - Normal Comment
    @Test
    void createComment_shouldReturn200_whenNormalCommentByMoimMember() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 0);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.commentId").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.content").value(requestDto.getContent()));
        resultActions.andExpect(jsonPath("$.data.depth").value(requestDto.getDepth()));
        resultActions.andExpect(jsonPath("$.data.parentId").value(requestDto.getParentId()));
        resultActions.andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));
    }


    // Post Comment 생성 요청 - Reply Comment
    @Test
    void createComment_shouldReturn200_whenReplyCommentByMoimMember() throws Exception {

        // given
        PostComment parentComment = makePostComment(moimCreator, testMoimPost, 0, null);
        em.persist(parentComment);
        em.flush();
        em.clear();

        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), parentComment.getId(), 1);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.commentId").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.content").value(requestDto.getContent()));
        resultActions.andExpect(jsonPath("$.data.depth").value(requestDto.getDepth()));
        resultActions.andExpect(jsonPath("$.data.parentId").value(requestDto.getParentId()));
        resultActions.andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

    }


    // Post Comment 중 답글 생성 요청인데, 부모라고 보낸 글이 댓글이 아니라 답글이였음 (2차 이상 답글 생성 시도)
    @Test
    void createComment_shouldReturn422_whenTrialToReplyOnChildComment_byMoimingApiException() throws Exception {

        // given
        PostComment parentComment = makePostComment(moimCreator, testMoimPost, 0, null);
        PostComment childComment = makePostComment(moimMember, testMoimPost, 1, parentComment);
        em.persist(parentComment);
        em.persist(childComment);
        em.flush();
        em.clear();

        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), childComment.getId(), 1); // child 댓글이 부모라고 들어가지게 됨
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_COMMENT_NOT_PARENT.getErrCode()));

    }


    // Validation 요청 오류 점검 1) postId null 2) depth null
    @Test
    void createComment_shouldReturn400_whenRequestValidationFails_byMoimingValidationException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(null, null, 0);
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
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(2)));

    }


    // Validation 요청 오류 점검 1) content over 100
    @Test
    void createComment_shouldReturn400_whenRequestValidationFailsByContentLength_byMoimingValidationException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 0);
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
    void createComment_shouldReturn404_whenNotMoimMemberRequest_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 0);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(notMoimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));
    }


    // Post Comment 생성 요청 오류 - Moim Member Not Active
    @Test
    void createComment_shouldReturn403_whenMoimMemberNotActiveRequest_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 0);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(inactiveMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_ACTIVE.getErrCode()));
    }


    // Post Comment 생성 요청 오류 - Parent Comment Not Found
    @Test
    void createComment_shouldReturn404_whenParentCommentNotFound_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), 1234L, 1);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_COMMENT_NOT_FOUND.getErrCode()));
    }


    // Post Comment 생성 요청 오류 - Parent Comment Mapping Error 하나만
    @Test
    void createComment_shouldReturn422_whenParentCommentMappingWrong_byMoimingApiException() throws Exception {

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 1);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_POST_COMMENT_CREATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_SITUATION.getErrCode()));

    }


    // UPDATE REQUEST TEST
    // 댓글 수정 성공 - content 정보만 수정한다 - 댓글 작성자
    @Test
    void updateComment_shouldReturn200_whenRequestByCommentCreator() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));
        System.out.println("response = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.commentId").value(prePostComment.getId()));
        resultActions.andExpect(jsonPath("$.data.content").value(requestDto.getContent()));
        resultActions.andExpect(jsonPath("$.data.depth").value(prePostComment.getDepth()));
        if (prePostComment.getParent() != null) {
            resultActions.andExpect(jsonPath("$.data.parentId").value(prePostComment.getParent().getId()));
        } else {
            resultActions.andExpect(jsonPath("$.data.parentId").doesNotExist()); // NULL 확인
        }
        resultActions.andExpect(jsonPath("$.data.updaterId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberId").value(moimMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.nickname").value(moimMember.getNickname()));

    }


    // 댓글 수정 실패 - 모임 생성자
    @Test
    void updateComment_shouldReturn403_whenRequestByMoimCreator_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimCreator, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));
        System.out.println("response = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));
    }


    // 댓글 수정 실패 - 모임 생성자
    @Test
    void updateComment_shouldReturn403_whenRequestByOtherMoimMember_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));
    }


    // 댓글 수정 실패 - 비모임원
    @Test
    void updateComment_shouldReturn404_whenRequestByNonMoimMember_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(notMoimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));
        System.out.println("response = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

    }


    // 댓글 수정 실패 - Inactive 모임원
    @Test
    void updateComment_shouldReturn403_whenRequestByInactiveMember_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(inactiveMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_ACTIVE.getErrCode()));

    }


    // commentId null validation
    @Test
    void updateComment_shouldReturn403_whenCommentIdNull_byMoimingValidationException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(null);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

    }


    // content null validation
    @Test
    void updateComment_shouldReturn400_whenContentNull_byMoimingValidationException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        requestDto.setContent(null);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // content over 100 validation
    @Test
    void updateComment_shouldReturn400_whenContentOver100_byMoimingValidationException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(prePostComment.getId());
        String contentOver100 = "a" + "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        requestDto.setContent(contentOver100);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // 존재하지 않는 Comment
    @Test
    void updateComment_shouldReturn404_whenPostCommentNotExist_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        PostCommentUpdateReqDto requestDto = makeCommentUpdateReqDto(1827L);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(moimMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_POST_COMMENT_UPDATE)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_COMMENT_NOT_FOUND.getErrCode()));

    }


    // DELETE REQUEST TEST (MoimPost 의 댓글 갯수도 같이 검증)
    // 댓글 삭제 성공 - content 와 hasDeleted 검증 - 댓글 작성자
    @Test
    void deleteComment_shouldReturn200_whenRequestByCommentCreator() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", prePostComment.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo("");
        assertThat(postComment.isHasDeleted()).isEqualTo(true);
        assertThat(postComment.getUpdaterId()).isEqualTo(moimMember.getId());

    }


    // 댓글 삭제 성공 - content 와 hasDeleted 검증 - 모임 생성자
    @Test
    void deleteComment_shouldReturn200_whenRequestByMoimCreator() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(moimCreator, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", prePostComment.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo("");
        assertThat(postComment.isHasDeleted()).isEqualTo(true);
        assertThat(postComment.getUpdaterId()).isEqualTo(moimCreator.getId());

    }


    // 댓글 삭제 실패 - 다른 일반 모임원의 삭제 요청
    @Test
    void deleteComment_shouldReturn403_whenRequestByOtherMoimMember_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(moimMember2, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", prePostComment.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));

        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo(prePostComment.getContent());
        assertThat(postComment.isHasDeleted()).isEqualTo(false);
        assertThat(postComment.getUpdaterId()).isNull();

    }


    // 댓글 삭제 실패 - 비모임원
    @Test
    void deleteComment_shouldReturn404_whenRequestByNonMoimMember_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(notMoimMember, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", prePostComment.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo(prePostComment.getContent());
        assertThat(postComment.isHasDeleted()).isEqualTo(false);
        assertThat(postComment.getUpdaterId()).isNull();

    }


    // 댓글 삭제 실패 - inActive 모임원
    @Test
    void deleteComment_shouldReturn403_whenRequestByInactiveMember_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(inactiveMember, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", prePostComment.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_ACTIVE.getErrCode()));

        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo(prePostComment.getContent());
        assertThat(postComment.isHasDeleted()).isEqualTo(false);
        assertThat(postComment.getUpdaterId()).isNull();

    }


    // URL 에 postCommentId 없음
    @Test
    void deleteComment_shouldReturn404_whenNoPostCommentId_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(inactiveMember, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));


        // then
        resultActions.andExpect(status().isNotFound()); // 없는 경로는 없다


        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo(prePostComment.getContent());
        assertThat(postComment.isHasDeleted()).isEqualTo(false);
        assertThat(postComment.getUpdaterId()).isNull();

    }


    // URL 에 moimPostId, moimId 없음
    @Test
    void deleteComment_shouldReturn404_whenMoimPostIdAndMoimIdEmpty_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(inactiveMember, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {"", "", prePostComment.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isNotFound()); // 없는 경로는 없다


        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo(prePostComment.getContent());
        assertThat(postComment.isHasDeleted()).isEqualTo(false);
        assertThat(postComment.getUpdaterId()).isNull();

    }


    // 존재하지 않는 Comment
    @Test
    void deleteComment_shouldReturn404_whenCommentNotFound_byMoimingApiException() throws Exception {

        // given
        createPostComment();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] beforeUrlParam = {"moimId", "moimPostId", "postCommentId"};
        String[] afterUrlParam = {testMoim.getId() + "", testMoimPost.getId() + "", 18384 + ""};


        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_POST_COMMENT_DELETE, beforeUrlParam, afterUrlParam))
                .header(HEADER, PREFIX + accessToken));


        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_POST_COMMENT_NOT_FOUND.getErrCode()));


        // then - db verify
        PostComment postComment = em.find(PostComment.class, prePostComment.getId());
        assertThat(postComment.getContent()).isEqualTo(prePostComment.getContent());
        assertThat(postComment.isHasDeleted()).isEqualTo(false);
        assertThat(postComment.getUpdaterId()).isNull();

    }


    @BeforeEach
    void su() {

        // Member moimMember, moimCreator
        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        moimMember2 = makeTestMember(memberEmail5, memberPhone5, memberName5, nickname5, ci5, testRole);
        notMoimMember = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        inactiveMember = makeTestMember(memberEmail4, memberPhone4, memberName4, nickname4, ci4, testRole);
        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(moimMember2);
        em.persist(notMoimMember);
        em.persist(inactiveMember);

        // Moim moim 존재
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);


        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember.memberJoinMoim(moimMember2, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember.memberJoinMoim(inactiveMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        em.persist(testMoim);

        // Post post 존재
        testMoimPost = makeMoimPost(testMoim, moimCreator, MoimPostCategory.NOTICE, false);
        em.persist(testMoimPost);

        em.flush();
        em.clear();

    }


    void createPostComment() {

        prePostComment = makePostComment(moimMember, testMoimPost, 0, null);
        em.persist(prePostComment);
        em.flush();
        em.clear();
    }
}