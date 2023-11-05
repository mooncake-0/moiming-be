package com.peoplein.moiming.controller;


import com.jayway.jsonpath.JsonPath;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    public final String MOIM_POST_BASE_URL = API_SERVER + API_MOIM_VER + API_MOIM;
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
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
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
                testMoim.getId(), normalPostTitle, normalPostcontent, MoimPostCategory.REVIEW, false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(MOIM_POST_BASE_URL + API_MOIM_POST + "/create").content(requestBody).contentType(MediaType.APPLICATION_JSON)
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
                null, normalPostTitle, normalPostcontent, MoimPostCategory.REVIEW, false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(MOIM_POST_BASE_URL + API_MOIM_POST + "/create").content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
    }


    // PostTitle 작음
    @Test
    void createPost_shouldReturn400_whenMoimTitleNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), null, normalPostcontent, MoimPostCategory.REVIEW, false, true
        );

        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(MOIM_POST_BASE_URL + API_MOIM_POST + "/create").content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
        resultActions.andExpect(jsonPath("$.data.postTitle").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // Post Content 작음 // PostTitle 과 같은 Validation 이므로, 한 상황씩 체크한다
    @Test
    void createPost_shouldReturn400_whenPostContentLessThan10_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, "내용은최소10자", MoimPostCategory.REVIEW, false, true
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(MOIM_POST_BASE_URL + API_MOIM_POST + "/create").content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
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
        ResultActions resultActions = mvc.perform(post(MOIM_POST_BASE_URL + API_MOIM_POST + "/create").content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // anyBoolean Null
    @Test
    void createPost_shouldReturn400_whenAnyBooleanValueNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, MoimPostCategory.REVIEW, false, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(MOIM_POST_BASE_URL + API_MOIM_POST + "/create").content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String body = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("body = " + body);


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

    }


    // 2번 - getMoimTest 일반조회 성공 - 각 URL 에 각 value 들이 없을때를 검증한다
    @Test
    void getMoimPosts_shouldReturn200AndRespDtos_whenRightInfoWithNoNullPassed() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 3000);
        Long moimId = testMoim.getId();
        MoimPostCategory category = MoimPostCategory.GREETING;
        int limit = 10;
        makeMoimPosts(20, testMoim, moimCreator, em);


        // given - getting LastPostId (첫 요청을 가져간다)
        List<MoimPost> tmps = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id and mp.moimPostCategory = :category " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("moim_id", moimId)
                .setParameter("category", category)
                .setMaxResults(10) // JPQL 은 페이징을 따로 주입한다
                .getResultList();
        Long lastPostId = tmps.get(tmps.size() - 1).getId();


        // when
        ResultActions resultActions = mvc.perform(get(MOIM_POST_BASE_URL + "/" + moimId + API_MOIM_POST)
                .param("lastPostId", lastPostId + "")
                .param("category", category + "")
                .param("limit", limit + "")
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken));


        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data").isArray()); // 비어있을 확률이 높지만 어쨌든 배열이 들어온다
        // 입하는 게시물 갯수를 엄청 늘리면 몇개 들어오는거 확인

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

        // when
        ResultActions resultActions = mvc.perform(get(MOIM_POST_BASE_URL + "/ " + API_MOIM_POST) // " " 으로 moimId 제공
                .param("lastPostId", samplePost.getId() + "") // 해당값 이후로 출력한다
                .param("category", category + "")
                .param("limit", limit + "")
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        //then
        resultActions.andExpect(status().isInternalServerError());
    }


    // 이하 모든 Test 들은 다음을 검증함 : Controller 단에서 문제가 되지 않는다 & Return Value 가 확인된다
    // lastPostId Null 이면 반환함 (첫 요청임)
    @Test
    void getMoimPosts_shouldReturn200AndRespDtos_whenFirstRequestAndLastPostIdNull() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 3000);
        Long moimId = testMoim.getId();
        MoimPostCategory category = MoimPostCategory.GREETING;
        int limit = 10;
        makeMoimPosts(20, testMoim, moimCreator, em);


        // when
        ResultActions resultActions = mvc.perform(get(MOIM_POST_BASE_URL + "/" + moimId + API_MOIM_POST)
                .param("category", category + "")
                .param("limit", limit + "")
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken));


        // then - query data prepare
        List<MoimPost> neededResults = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id and mp.moimPostCategory = :category " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("moim_id", moimId)
                .setParameter("category", category)
                .setMaxResults(10) // JPQL 은 페이징을 따로 주입한다
                .getResultList();


        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data").isArray());
        if (!neededResults.isEmpty()) { // 미리 준비해둔 반환결과에 따라 검증을 나눈다
            resultActions.andExpect(jsonPath("$.data[*].moimPostCategory.value", everyItem(is(category))));
        }

    }


    // category Null 이면 반환함 - category ID = NULL 가능성 Test 도 있으니 Null 로 진행
    @Test
    void getMoimPosts_shouldReturn200AndRespDtos_whenCategoryNull() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 3000);
        Long moimId = testMoim.getId();
        int limit = 10;
        makeMoimPosts(20, testMoim, moimCreator, em);


        // when
        ResultActions resultActions = mvc.perform(get(MOIM_POST_BASE_URL + "/" + moimId + API_MOIM_POST)
                .param("limit", limit + "")
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken));


        // then - query data prepare
        List<MoimPost> neededResults = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("moim_id", moimId)
                .setMaxResults(10) // JPQL 은 페이징을 따로 주입한다
                .getResultList();

        // then prepare- json looping 확인하는 방법 result String 필요 - 원하는 값이 다 들어있음을 증명
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(responseBody, "$.data[*].moimPostId"); // Json 에 L 없이 들어가므로 L 이 빠져서 Integer 로 저장된다
        List<Long> longIds = ids.stream().map(id -> (long) id).collect(Collectors.toList()); // 비교를 위해 Long List 로 변환하여 준비

        // then - 일반 응답 비교
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data").isArray());

        // then - 두 비교 리스트의 반환 결과는 동일
        assertThat(longIds.size()).isEqualTo(neededResults.size());
        for (MoimPost neededResult : neededResults) {
            assertThat(longIds).contains(neededResult.getId());
        }
    }

}


// limit 은 null 일 수 없고, 안들어오면 default 10 으로 동작함
// 비회원이 요청하면 받아온 애들의 privateVisibility 는 모두 false 이다
