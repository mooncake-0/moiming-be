package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.ContentStatus;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.repository.MoimCountRepository;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.AreaValue.*;
import static com.peoplein.moiming.domain.enums.CategoryName.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestParams.*;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimSuggestControllerTest extends TestObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private MoimCountRepository moimCountRepository;

    private Member member1, member2, member3, member4, member5;
    private Moim moim1, moim2, moim3, moim4, moim5, moim6, moim7, moim8, moim9, moim10;
    private MoimMonthlyCount moim4Count;

    private String moim1Name = "라틴어를 강남에서 배우는 공간";
    private String moim2Name = "서울사는 사람들";
    private String moim3Name = "강아지들 모여라";
    private String moim4Name = "우리집 반려동물";
    private String moim5Name = "프로그래밍 스터디";
    private String moim6Name = "강아지 카메라 찍는 사람들";
    private String moim7Name = "여행 여기저기 다녀보자";
    private String moim8Name = "적합한 직무 찾기";
    private String moim9Name = "우리는 누구의 사람들이 사는가";
    private String moim10Name = "집에 너무 가고 싶은 사람들";

    private Area moim1Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim2Area = new Area(STATE_SEOUL, CITY_GANGDONG);
    private Area moim3Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim4Area = new Area(STATE_SEOUL, CITY_GANGDONG);
    private Area moim5Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim6Area = new Area(STATE_SEOUL, CITY_GANGDONG);
    private Area moim7Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim8Area = new Area(STATE_SEOUL, CITY_GANGDONG);
    private Area moim9Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim10Area = new Area(STATE_SEOUL, CITY_GANGDONG);


    // 실패 - offset 0 이 아닌 다른 값이 들어 있음
    @Test
    void getSuggestedMoim_shouldReturn400_whenOffsetNotZero_byMoimingApiException() throws Exception {

        // given
        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        em.persist(member1);

        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .param("offset", "1")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 실패 - 1차 카테고리 필터링 시도 / 현재 필터는 요구사항에 없음
    @Test
    void getSuggestedMoim_shouldReturn422_whenDepth0CategoryFilterPassed_byMoimingApiException() throws Exception {

        // given
        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        em.persist(member1);

        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .param("categoryFilter", "댄스/무용")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_SITUATION.getErrCode()));

    }


    // 지역 필터값이 이상해서 Filter Enum 과 매핑 실패
    @Test
    void getSuggestedMoim_shouldReturn400_whenAreaFilterMapFail_byMoimingApiException() throws Exception {

        // given
        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        em.persist(member1);

        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .param("areaFilter", "잘못된카테고리")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 카테고리 필터값이 이상해서 Filter Enum 과 매핑 실패
    @Test
    void getSuggestedMoim_shouldReturn400_whenCategoryFilterMapFail_byMoimingApiException() throws Exception {

        // given
        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        em.persist(member1);

        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .param("categoryFilter", "잘못된카테고리")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 성공 - 인기순을 불러온다 - Top 5 를 불러온다 - moim1, moim2, moim4, moim3, moim6
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop5() throws Exception {

        // given
        suData();
        suCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "0")
                .param("limit", "5"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(5)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim6.getId()));

    }


    // 성공 - 인기순을 불러온다 - Top 10 를 불러온다 - moim9, moim10 가 없음을 확인 순서 확인 - moim1, moim2, moim4, moim3, moim6, moim8, moim7, moim5
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10() throws Exception {

        // given
        suData();
        suCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(8)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimId").value(moim5.getId()));

    }

    // 성공 - 인기순을 불러온다 - Top 10 를 불러온다 - moim9, moim10 가 없음을 확인 순서 확인 - moim1, moim2, moim4, moim3, moim6, moim8, moim7, moim5
    // 단, 가장 인기 높은 모임 3가지 추가 - 삭제된 모임, 비활성화된 모임, 정상 모임 - 2가지는 조회 안되고, 1가지 정상 조회됨을 확인
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenDeletedAndHiddenMoimExists() throws Exception {

        // given
        suData();
        suCounting();

        // 비활성화된 모임들 추가
        Category tmpCategory1 = new Category(1L, fromValue(DANCE.getValue()), 0, null);
        Category tmpCategory2 = new Category(2L, fromValue(LATIN_DANCE.getValue()), 1, tmpCategory1);
        List<Category> tmpCategories = List.of(tmpCategory1, tmpCategory2);

        Moim deletedMoim = makeTestMoim("가장 조회수 높았던 삭제된 모임", 10, moim1Area, tmpCategories, member1, 1974, 2004, MemberGender.N); //1
        deletedMoim.minusCurMemberCount(); // 한 명나가서, 삭제라고 인식하도록 만듬
        em.persist(deletedMoim);

        Moim hiddenMoim = makeTestMoim("가장 조회수 높았던 신고 숨김처리된 모임", 10, moim1Area, tmpCategories, member1, 1974, 2004, MemberGender.N); //1
        hiddenMoim.changeContentStatus(ContentStatus.HIDDEN, 1000L);
        em.persist(hiddenMoim);

        // 비활성화 때문에 제외되는게 맞는지 확인하기 위해 정상 추가
        Moim normalMoim = makeTestMoim("가장 조회수 높은 정상 모임", 10, moim1Area, tmpCategories, member1, 1974, 2004, MemberGender.N); //1
        em.persist(normalMoim);

        // 위 세개의 모임은 가장 조회수가 높다 (높았다)
        MoimMonthlyCount deletedMoimCount = MoimMonthlyCount.createMoimMonthlyCount(deletedMoim);
        MoimMonthlyCount hiddenMoimCount = MoimMonthlyCount.createMoimMonthlyCount(hiddenMoim);
        MoimMonthlyCount normalMoimCount = MoimMonthlyCount.createMoimMonthlyCount(normalMoim);
        em.persist(deletedMoimCount);
        em.persist(hiddenMoimCount);
        em.persist(normalMoimCount);

        increaseMoimMonthCount(deletedMoimCount, 100);
        increaseMoimMonthCount(hiddenMoimCount, 100);
        increaseMoimMonthCount(normalMoimCount, 100);

        em.flush();
        em.clear();

        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(9)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(normalMoim.getId())); // 정상 조회
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[8].moimId").value(moim5.getId()));

    }


    // 성공 - 인기순이 바뀐다 - moim4 가 3번 count 가 증가한 후 다시 요청 - moim4 가 먼저 온다
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10AfterMoim4Inc3() throws Exception {

        // given
        suData();
        suCounting();

        // given - add data
        moim4Count = em.find(MoimMonthlyCount.class, moim4Count.getId());
        increaseMoimMonthCount(moim4Count, 3);
        em.flush();
        em.clear();
        String accessToken = createTestJwtToken(member1, 2000);


        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(8)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimId").value(moim5.getId()));

    }


    // 성공 - moim9 가 count 가 생겨서 2로 는다
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10AfterMoim2Inc2() throws Exception {

        // given
        suData();
        suCounting();

        // given - add data
        MoimMonthlyCount moim9Count = MoimMonthlyCount.createMoimMonthlyCount(moim9);
        increaseMoimMonthCount(moim9Count, 2);
        em.persist(moim9Count);
        em.flush();
        em.clear();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(9)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimId").value(moim5.getId()));
        resultActions.andExpect(jsonPath("$.data[8].moimId").value(moim9.getId()));

    }


    // 성공 - Top10 을 불러온다 - areaFilterOn - 서울시 전체
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10WithAreaFilterSeoul() throws Exception {

        // given
        suData();
        suCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("areaFilter", "서울")
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(8)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimId").value(moim5.getId()));

    }


    // 성공 - Top10 을 불러온다 - areaFilterOn - 강남구
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10WithAreaFilterGangNam() throws Exception {

        // given
        suData();
        suCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("areaFilter", "강남구")
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(4)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim5.getId()));

    }


    // 성공 - Top10 을 불러온다 - categoryFilterOn
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10WithCategoryFilter() throws Exception {

        // given
        suData();
        suCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("categoryFilter", "해외여행")
                .param("offset", "0")
                .param("limit", "10"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(4)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim8.getId()));

    }


    // 성공 - Top10 을 불러온다 - area&categoryFilter ON
    @Test
    void getSuggestedMoim_shouldReturn200WithData_whenNoLastMonthCountReqTop10WithBothFilterOn() throws Exception {
        // given
        suData();
        suCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("areaFilter", "강동구")
                .param("categoryFilter", "라틴댄스")
                .param("offset", "0")
                .param("limit", "10"));


        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim4.getId()));

    }


    // 두달 집계 쿼리
    // 성공 - 두 달에 대한 집계 후 데이터를 들고온다
    @Test
    void getSuggestedMoimTest_shouldReturn200WithResponse_whenTwoMonthReqTop10() throws Exception {

        /*
               지난달   |  이번달  |  합계
         moim1  3     |   11       14  라틴어를 강남에서 배우는 공간    강남    라틴댄스
         moim2  4     |   10       14  "서울사는 사람들";    강동    해외여행
         moim3  1     |   7        8  "강아지들 모여라";    강남    해외여행
         moim4  2     |   9        11  "우리집 반려동물";    강동    라틴댄스
         moim5  6     |   4        10  "프로그래밍 스터디";    강남    라틴댄스
         moim6  4     |   6        10  "강아지 카메라 찍는 사람들";    강동    해외여행
         moim7  10    |   5        15  "여행 여기저기 다녀보자";    강남    라틴댄스
         moim8  8     |   5        13  "적합한 직무 찾기";    강동    해외여행

         > 순서 : 7 > 2 > 1 > 8 > 4 > 6 > 5 > 3
         */

        // given
        suData();
        suCounting();
        suLastMonthCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "0")
                .param("limit", "10"));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(8)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[6].moimId").value(moim5.getId()));
        resultActions.andExpect(jsonPath("$.data[7].moimId").value(moim3.getId()));

    }


    // 성공 - 지역 필터 낌
    @Test
    void getSuggestedMoimTest_shouldReturn200WithResponse_whenTwoMonthReqTop10WithAreaFilter() throws Exception {

        /*
               지난달   |  이번달  |  합계
         moim1  3     |   11       14  라틴어를 강남에서 배우는 공간    강남    라틴댄스
         moim2  4     |   10       14  "서울사는 사람들";    강동    해외여행
         moim3  1     |   7        8  "강아지들 모여라";    강남    해외여행
         moim4  2     |   9        11  "우리집 반려동물";    강동    라틴댄스
         moim5  6     |   4        10  "프로그래밍 스터디";    강남    라틴댄스
         moim6  4     |   6        10  "강아지 카메라 찍는 사람들";    강동    해외여행
         moim7  10    |   5        15  "여행 여기저기 다녀보자";    강남    라틴댄스
         moim8  8     |   5        13  "적합한 직무 찾기";    강동    해외여행

         > 순서 : 7 > 1 > 5 > 3
         */

        // given
        suData();
        suCounting();
        suLastMonthCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("areaFilter", "강남구")
        );

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(4)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim5.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));

    }


    // 성공 - 카테고리 필터 낌
    @Test
    void getSuggestedMoimTest_shouldReturn200WithResponse_whenTwoMonthReqTop10WithCategoryFilter() throws Exception {

        /*
               지난달   |  이번달  |  합계
         moim1  3     |   11       14  라틴어를 강남에서 배우는 공간    강남    라틴댄스
         moim2  4     |   10       14  "서울사는 사람들";    강동    해외여행
         moim3  1     |   7        8  "강아지들 모여라";    강남    해외여행
         moim4  2     |   9        11  "우리집 반려동물";    강동    라틴댄스
         moim5  6     |   4        10  "프로그래밍 스터디";    강남    라틴댄스
         moim6  4     |   6        10  "강아지 카메라 찍는 사람들";    강동    해외여행
         moim7  10    |   5        15  "여행 여기저기 다녀보자";    강남    라틴댄스
         moim8  8     |   5        13  "적합한 직무 찾기";    강동    해외여행

         > 순서 :  2  > 8  > 6 > 3
         */

        // given
        suData();
        suCounting();
        suLastMonthCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("categoryFilter", "해외여행"));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(4)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim8.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim3.getId()));

    }


    // 성공 - 지역필터, 카테고리 필터 모두 낌
    @Test
    void getSuggestedMoimTest_shouldReturn200WithResponse_whenTwoMonthReqTop10WithBothFilter() throws Exception {

        /*
               지난달   |  이번달  |  합계
         moim1  3     |   11       14  라틴어를 강남에서 배우는 공간    강남    라틴댄스
         moim2  4     |   10       14  "서울사는 사람들";    강동    해외여행
         moim3  1     |   7        8  "강아지들 모여라";    강남    해외여행
         moim4  2     |   9        11  "우리집 반려동물";    강동    라틴댄스
         moim5  6     |   4        10  "프로그래밍 스터디";    강남    라틴댄스
         moim6  4     |   6        10  "강아지 카메라 찍는 사람들";    강동    해외여행
         moim7  10    |   5        15  "여행 여기저기 다녀보자";    강남    라틴댄스
         moim8  8     |   5        13  "적합한 직무 찾기";    강동    해외여행

         > 순서 : 7 > 1  > 5
         */

        // given
        suData();
        suCounting();
        suLastMonthCounting();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_SUGGESTED)
                .header(HEADER, PREFIX + accessToken)
                .param("areaFilter", "강남구")
                .param("categoryFilter", "라틴댄스")
        );

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim7.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim1.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim5.getId()));

    }


    void suCounting() {

        // moim8 까지 count 가 있다고 세팅하자
        MoimMonthlyCount moim1Count = MoimMonthlyCount.createMoimMonthlyCount(moim1);
        MoimMonthlyCount moim2Count = MoimMonthlyCount.createMoimMonthlyCount(moim2);
        MoimMonthlyCount moim3Count = MoimMonthlyCount.createMoimMonthlyCount(moim3);
        moim4Count = MoimMonthlyCount.createMoimMonthlyCount(moim4);
        MoimMonthlyCount moim5Count = MoimMonthlyCount.createMoimMonthlyCount(moim5);
        MoimMonthlyCount moim6Count = MoimMonthlyCount.createMoimMonthlyCount(moim6);
        MoimMonthlyCount moim7Count = MoimMonthlyCount.createMoimMonthlyCount(moim7);
        MoimMonthlyCount moim8Count = MoimMonthlyCount.createMoimMonthlyCount(moim8);
        em.persist(moim1Count);
        em.persist(moim2Count);
        em.persist(moim3Count);
        em.persist(moim4Count);
        em.persist(moim5Count);
        em.persist(moim6Count);
        em.persist(moim7Count);
        em.persist(moim8Count);

        increaseMoimMonthCount(moim1Count, 10);
        increaseMoimMonthCount(moim2Count, 9);
        increaseMoimMonthCount(moim3Count, 6);
        increaseMoimMonthCount(moim4Count, 8);
        increaseMoimMonthCount(moim5Count, 3);
        increaseMoimMonthCount(moim6Count, 5);
        increaseMoimMonthCount(moim7Count, 4);
        increaseMoimMonthCount(moim8Count, 4);

        // 인기 순서 : moim1, moim2, moim4, moim3, moim6, moim8, moim7, moim5 // (7,8 은 최신 생성일)


        em.flush();
        em.clear();

    }


    // Java 객체로는 Test 생성이 어려우므로, JPQL 을 통해 주입해서 사용한다
    // 지난달 모임 count 에 대한 정보도 넣어준다
    void suLastMonthCounting() {
        LocalDate lastMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        runQuery(lastMonth, moim1.getId(), 3, 200L);
        runQuery(lastMonth, moim2.getId(), 4, 201L);
        runQuery(lastMonth, moim3.getId(), 1, 202L);
        runQuery(lastMonth, moim4.getId(), 2, 203L);
        runQuery(lastMonth, moim5.getId(), 6, 204L);
        runQuery(lastMonth, moim6.getId(), 4, 205L);
        runQuery(lastMonth, moim7.getId(), 10, 206L);
        runQuery(lastMonth, moim8.getId(), 8, 207L);
    }


    void runQuery(LocalDate countDate, Long moimId, int monthlyCount, Long id) {

        String query = "INSERT INTO moim_monthly_count (count_date, moim_id, monthly_count, moim_monthly_count_id) " +
                "VALUES (:count_date, :moim_id, :monthly_count, :moim_monthly_count_id)";

        em.createNativeQuery(query)
                .setParameter("count_date", countDate)
                .setParameter("moim_id", moimId)
                .setParameter("monthly_count", monthlyCount)
                .setParameter("moim_monthly_count_id", id)
                .executeUpdate();

    }


    void increaseMoimMonthCount(MoimMonthlyCount count, int n) {
        for (int i = 0; i < n; i++) {
            count.increaseMonthlyCount();
        }
    }


    void suData() {

        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        member2 = makeTestMember(memberPhone2, nickname2, ci2, role);
        member3 = makeTestMember(memberPhone3, nickname3, ci3, role);
        member4 = makeTestMember(memberPhone4, nickname4, ci4, role);
        member5 = makeTestMember(memberPhone5, nickname5, ci5, role);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);

        // 가입조건에 부합하게 생성될 수 있도록 지정
        MemberInfo member1Info = makeMemberInfo(member1, memberGender, memberBirth);
        em.persist(member1Info);
        MemberInfo member2Info = makeMemberInfo(member2, MemberGender.F, LocalDate.of(2000, 1, 1));
        em.persist(member2Info);
        MemberInfo member3Info = makeMemberInfo(member3, memberGender, LocalDate.of(2000, 1, 1));
        em.persist(member3Info);
        MemberInfo member4Info = makeMemberInfo(member4, MemberGender.F, LocalDate.of(2000, 1, 1));
        em.persist(member4Info);
        MemberInfo member5Info = makeMemberInfo(member5, memberGender, memberBirth);
        em.persist(member5Info);


        Category testCategory1 = new Category(1L, fromValue(DANCE.getValue()), 0, null);
        Category testCategory1_1 = new Category(2L, fromValue(LATIN_DANCE.getValue()), 1, testCategory1);
        Category testCategory2 = new Category(3L, fromValue(OUTDOOR.getValue()), 0, null);
        Category testCategory2_1 = new Category(4L, fromValue(INTERNATIONAL.getValue()), 1, testCategory2);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        em.persist(testCategory2);
        em.persist(testCategory2_1);

        List<Category> moim1Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim2Category = List.of(testCategory2, testCategory2_1);
        List<Category> moim3Category = List.of(testCategory2, testCategory2_1);
        List<Category> moim4Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim5Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim6Category = List.of(testCategory2, testCategory2_1);
        List<Category> moim7Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim8Category = List.of(testCategory2, testCategory2_1);
        List<Category> moim9Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim10Category = List.of(testCategory1, testCategory1_1);


        moim1 = makeTestMoim(moim1Name, 10, moim1Area, moim1Category, member1, 1974, 1999, MemberGender.N); //1 // 25 ~ 50
        moim2 = makeTestMoim(moim2Name, 10, moim2Area, moim2Category, member2, 1984, 2004, MemberGender.F); //2
        moim3 = makeTestMoim(moim3Name, 10, moim3Area, moim3Category, member3, -1, -1, MemberGender.N);
        moim4 = makeTestMoim(moim4Name, 10, moim4Area, moim4Category, member4, 1984, 2004, MemberGender.F); //2
        moim5 = makeTestMoim(moim5Name, 10, moim5Area, moim5Category, member5, -1, -1, MemberGender.N);
        moim6 = makeTestMoim(moim6Name, 10, moim6Area, moim6Category, member1, -1, -1, MemberGender.N);
        moim7 = makeTestMoim(moim7Name, 10, moim7Area, moim7Category, member2, 1999, 2006, MemberGender.N); //3 //18 ~ 25
        moim8 = makeTestMoim(moim8Name, 10, moim8Area, moim8Category, member3, 1999, 2006, MemberGender.N); //3
        moim9 = makeTestMoim(moim9Name, 10, moim9Area, moim9Category, member4, -1, -1, MemberGender.N);
        moim10 = makeTestMoim(moim10Name, 10, moim10Area, moim10Category, member5, -1, -1, MemberGender.N);

        em.persist(moim1);
        em.persist(moim2);
        em.persist(moim3);
        em.persist(moim4);
        em.persist(moim5);
        em.persist(moim6);
        em.persist(moim7);
        em.persist(moim8);
        em.persist(moim9);
        em.persist(moim10);

        em.flush();
        em.clear();
    }
}
