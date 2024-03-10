package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimPostPagingControllerTest extends TestObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member moimCreator, moimMember, notMoimMember;
    private Moim moim;
    private MoimPost post1, post2, post3, post4, post5, post6, post7, post8, post9, post10;
    private MoimPostCategory post1Category = MoimPostCategory.GREETING;
    private MoimPostCategory post2Category = MoimPostCategory.NOTICE;
    private MoimPostCategory post3Category = MoimPostCategory.GREETING;
    private MoimPostCategory post4Category = MoimPostCategory.EXTRA;
    private MoimPostCategory post5Category = MoimPostCategory.NOTICE;
    private MoimPostCategory post6Category = MoimPostCategory.REVIEW;
    private MoimPostCategory post7Category = MoimPostCategory.GREETING;
    private MoimPostCategory post8Category = MoimPostCategory.REVIEW;
    private MoimPostCategory post9Category = MoimPostCategory.EXTRA;
    private MoimPostCategory post10Category = MoimPostCategory.EXTRA;


    // 제어할 수 있는 조건들 lastPostId (커서), category 조건, MoimMember 의 요청 여부 // limit 조건
    // 페이징 갯수 제어해서 Post 갯수 제한해서 Test 하자

    private void dataSu() {

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
        moim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, moim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember.memberJoinMoim(notMoimMember, moim, MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        em.persist(moim);

        em.flush();
        em.clear();
    }


    private void postSu() {

        post1 = makeMoimPost(moim, moimCreator, post1Category, false);
        post2 = makeMoimPost(moim, moimCreator, post2Category, false);
        post3 = makeMoimPost(moim, moimMember, post3Category, false);
        post4 = makeMoimPost(moim, moimCreator, post4Category, true);
        post5 = makeMoimPost(moim, moimMember, post5Category, false);
        post6 = makeMoimPost(moim, moimCreator, post6Category, false);
        post7 = makeMoimPost(moim, notMoimMember, post7Category, false);
        post8 = makeMoimPost(moim, notMoimMember, post8Category, true);
        post9 = makeMoimPost(moim, moimMember, post9Category, false);
        post10 = makeMoimPost(moim, moimCreator, post10Category, true);

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);
        em.persist(post4);
        em.persist(post5);
        em.persist(post6);
        em.persist(post7);
        em.persist(post8);
        em.persist(post9);
        em.persist(post10);

        em.flush();
        em.clear();
    }


    // TC 들
    // 실패 : lastMoimPost 가 이상함 - 굳이 데이터 준비도 필요 없다
    @Test
    void getMoimPosts_shouldReturn404_whenLastMoimPostNotFound_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("lastPostId", "1234")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.MOIM_POST_NOT_FOUND.getErrCode()));
    }


    // 실패 - 요청 Category Filter 가 Mapping 에 실패함
    @Test
    void getMoimPosts_shouldReturn400_whenPostCategoryMapFail_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("lastPostId", "1234")
                .param("category", "잘못된카테고리")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_INVALID_REQUEST_PARAM.getErrCode()));
    }


    // 이후 성공
    // 멤버의 요청 - 20개 요청 - 10개 다 들고옴
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqDefault() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(10)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post10.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post9.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post8.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimPostId").value(post7.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimPostId").value(post6.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimPostId").value(post5.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimPostId").value(post4.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimPostId").value(post3.getId()));
        resultActions.andExpect(jsonPath("$.data[8].moimPostId").value(post2.getId()));
        resultActions.andExpect(jsonPath("$.data[9].moimPostId").value(post1.getId()));
    }


    // 멤버의 요청 자유글 필터 요청 - 20개 요청 - 3개 들고옴
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqWithCategoryFilter() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("category", MoimPostCategory.EXTRA.getValue())
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post10.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post9.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post4.getId()));
    }


    // 멤버의 페이징 요청 - 우선 5개 요청후, 뒤에 20개 요청
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqFirst5() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("limit", "5")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(5)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post10.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post9.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post8.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimPostId").value(post7.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimPostId").value(post6.getId()));
    }


    // 일반 요청시 lastPostId 를 주고 후속 요청 - 앞 테스트 후속 요청으로 잡음
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqWithLastPostId() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("lastPostId", post6.getId() + "")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(5)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post5.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post4.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post3.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimPostId").value(post2.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimPostId").value(post1.getId()));
    }


    // 멤버의 페이징 요청 - 후속 요청 중 새로운 Post 가 발생해도, 정상적으로 뒤에 5개를 반환한다
    @Test
    void getMoimPosts_shouldReturn200WithResponseAsPlanned_whenReqWithLastPostIdButNewMoimCreatedBetweenPaging() throws Exception {

        // given - data input
        dataSu();
        postSu();

        // 우선 5 개가 요청된 상황이고, 현재 같은 요청 기준에서 뒤에를 요청하다가 새로운 게시물이 생성되었다.
        // 이 때, 커서 기반 페이징은 영향을 받지 않아야 한다
        MoimPost morePost = makeMoimPost(moim, moimMember, MoimPostCategory.EXTRA, false);
        em.persist(morePost);
        em.flush();
        em.clear();

        // given
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("lastPostId", post6.getId() + "")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(5)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post5.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post4.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post3.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimPostId").value(post2.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimPostId").value(post1.getId()));
    }


    // 멤버의 페이징 + 필터 요청 - 우선 1개 요청이 되었었고, 이제 뒤에 2개 요청
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqWithFilterWithLastPostId() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(moimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("category", MoimPostCategory.EXTRA.getValue())
                .param("lastPostId", post10.getId() + "")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post9.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post4.getId()));
    }


    // 비멤버로 동일하게 진행
    // 비멤버의 요청 - 20개 요청 - 7개 다 들고옴
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqByNotMoimMember() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(notMoimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(7)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post9.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post7.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post6.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimPostId").value(post5.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimPostId").value(post3.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimPostId").value(post2.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimPostId").value(post1.getId()));

    }


    // 비멤버의 요청 자유글 필터 요청 - 20개 요청 - 3개 들고옴
    // GREETING 중에 비공개가 없음 - 다 공개
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqWithCategoryFilterByNotMoimMember() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(notMoimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("category", MoimPostCategory.GREETING.getValue())
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post7.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post3.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post1.getId()));
    }


    // 멤버의 페이징 요청 - 우선 5개 요청후, 뒤에 20개 요청
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqFirst5ByNotMoimMember() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(notMoimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("limit", "5")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(5)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post9.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post7.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimPostId").value(post6.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimPostId").value(post5.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimPostId").value(post3.getId()));
    }


    // 일반 요청시 lastPostId 를 주고 후속 요청 - 앞 테스트 후속 요청으로 잡음
    @Test
    void getMoimPosts_shouldReturn200WithResponse_whenReqWithLastPostIdByNotMoimMember() throws Exception {

        // given
        dataSu();
        postSu();
        String accessToken = createTestJwtToken(notMoimMember, 2000);
        String[] params = {"moimId"};
        String[] vals = {moim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals))
                .param("lastPostId", post3.getId() + "")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimPostId").value(post2.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimPostId").value(post1.getId()));
    }

}