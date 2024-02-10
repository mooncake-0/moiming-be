package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.security.token.JwtParams;
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
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// 로직 이슈는 다 뒤에서 점검되었다, Controller Test 만 진행되면 됨
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimPostControllerTest extends TestObjectCreator {

    private final String normalPostTitle = "제목입니다";
    private final String normalPostcontent = "내용은내용과내용입니다";
    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member moimCreator;
    private Member moimMember;
    private Member notMoimMember;
    private Moim testMoim;


    @BeforeEach
    void be() {

        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        notMoimMember = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(notMoimMember);

        // Moim Cateogry 저장
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        // Moim 준비
        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMoim);

        em.flush();
        em.clear();

    }


    // 1번 createPost() - 생성 성공 확인
    @Test
    void createPost_shouldReturn200_whenRightInfoPassed() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, MoimPostCategory.REVIEW.getValue(), false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("response = " + response);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));

    }


    // moimId Null
    @Test
    void createPost_shouldReturn400_whenMoimIdNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                null, normalPostTitle, normalPostcontent, MoimPostCategory.REVIEW.getValue(), false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
    }


    // PostTitle 작음
    @Test
    void createPost_shouldReturn400_whenMoimTitleNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), null, normalPostcontent, MoimPostCategory.REVIEW.getValue(), false, true
        );

        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.postTitle").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // Post Content 작음 // PostTitle 과 같은 Validation 이므로, 한 상황씩 체크한다
    @Test
    void createPost_shouldReturn400_whenPostContentLessThan10_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, "내용은최소10자", MoimPostCategory.REVIEW.getValue(), false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.postContent").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // MPC Null
    @Test
    void createPost_shouldReturn400_whenCategoryValueNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, null, false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // anyBoolean Null
    @Test
    void createPost_shouldReturn400_whenAnyBooleanValueNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, MoimPostCategory.REVIEW.getValue(), false, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String body = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("body = " + body);


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

    }


    // moimId Null 이면 Exception (뒤에서 발생함)
    @Test
    void getMoimPosts_shouldReturn400_whenMoimIdNullURL_byMoimingApiException() throws Exception {

        // given - 다른 상황 때문이 아님을 검증하기 위해 동일하게 SU
        String testToken = createTestJwtToken(moimMember, 3000);
        MoimPostCategory category = MoimPostCategory.GREETING;
        MoimPost samplePost = MoimPost.createMoimPost(normalPostTitle, normalPostcontent, category, false, false, testMoim, moimCreator);
        int limit = 10;
        em.persist(samplePost);
        em.flush();
        em.clear();

        String[] params = {"moimId"};
        String[] vals = {" "};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals)) // " " 으로 moimId 제공
                .param("lastPostId", samplePost.getId() + "") // 해당값 이후로 출력한다
                .param("category", category + "")
                .param("limit", limit + "")
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        //then
        resultActions.andExpect(status().isInternalServerError());
    }

}
