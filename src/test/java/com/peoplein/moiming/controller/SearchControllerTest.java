package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.AreaValue.*;
import static com.peoplein.moiming.domain.enums.CategoryName.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SearchControllerTest extends TestObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member1, member2, member3, member4, member5;
    private Moim moim1, moim2, moim3, moim4, moim5, moim6, moim7, moim8, moim9, moim10;

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

    private Area moim1Area = new Area(STATE_SEOUL.getName(), CITY_GANGNAM.getName());
    private Area moim2Area = new Area(STATE_SEOUL.getName(), CITY_GANGDONG.getName());
    private Area moim3Area = new Area(STATE_SEOUL.getName(), CITY_GANGNAM.getName());
    private Area moim4Area = new Area(STATE_SEOUL.getName(), CITY_GANGSEO.getName());
    private Area moim5Area = new Area(STATE_SEOUL.getName(), CITY_GWANAK.getName());
    private Area moim6Area = new Area(STATE_SEOUL.getName(), CITY_GWANGJIN.getName());
    private Area moim7Area = new Area(STATE_SEOUL.getName(), CITY_GURO.getName());
    private Area moim8Area = new Area(STATE_SEOUL.getName(), CITY_GEUMCHEON.getName());
    private Area moim9Area = new Area(STATE_SEOUL.getName(), CITY_NOWON.getName());
    private Area moim10Area = new Area(STATE_SEOUL.getName(), CITY_DOBONG.getName());


    // 실패 테스트
    // 필수 Param 없음 - 검색어 없거나 공백임
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithBlankKeyword_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "    ")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.SEARCH_KEYWORD_LENGTH_INVALID.getErrCode()));

    }


    // 지역 필터값이 이상해서 Filter Enum 과 매핑 실패
    @Test
    void searchMoim_shouldReturn400_whenAreaFilterMapFail_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "오류날겁니다")
                .param("offset", "0")
                .param("areaFilter", "이상한지역")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 카테고리 필터값이 이상해서 Filter Enum 과 매핑 실패
    @Test
    void searchMoim_shouldReturn400_whenCategoryFilterMapFail_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "오류날겁니다")
                .param("offset", "0")
                .param("categoryFilter", "이상한카테고리")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 필수 Param 없음 - offset 없음 - OFFSET 제거됨
//    @Test
//    void searchMoim_shouldReturn400_whenSearchedWithNoOffset_byMoimingApiException() throws Exception {
//
//         given
//        suData();
//        String accessToken = createTestJwtToken(member1, 2000);
//
//         when
//        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
//                .param("keyword", "오류날겁니다")
//                .header(HEADER, PREFIX + accessToken));
//
//        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
//
//         then
//        resultActions.andExpect(status().isBadRequest());
//        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_INVALID_REQUEST_PARAM.getErrCode()));
//
//    }


    // 필수 Param 오류 - sortBy 에 date 이외의 값이 들어옴
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithWrongSortBy_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "오류날겁니다")
//                .param("offset", "0")
                .param("sortBy", "FAMOUS_ORDER")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 1자 검색
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithOneKeyword_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "검")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.SEARCH_KEYWORD_LENGTH_INVALID.getErrCode()));

    }


    // 1자 + 공백 검색
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithOneKeywordWithBlank_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "검 ")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.SEARCH_KEYWORD_LENGTH_INVALID.getErrCode()));

    }


    // 20자 초과 검색
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithKeywordLengthOver20_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "이건20자를넘는그런검색어입니다그런검색어")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.SEARCH_KEYWORD_LENGTH_INVALID.getErrCode()));

    }


    // 모음 포함 검색
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithKeywordConsonant_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "자음이ㅍ함됨")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.SEARCH_KEYWORD_INVALID.getErrCode()));

    }


    // 자음 포함 검색
    @Test
    void searchMoim_shouldReturn400_whenSearchedWithKeywordVowel_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "모음ㅣ포함됨")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.SEARCH_KEYWORD_INVALID.getErrCode()));

    }


    // 1차 테스트 - 검색어 '라틴' - 검색 결과 : moim1 (제목), moim7 (카테고리)
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedLatin() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "라틴")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim7.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim1.getId()));

    }


    // 2차 테스트 - 검색어 '서울' - 검색 결과 : moim2 (제목) - 검색어로 매핑된 1차 지역 (도/시) 는 검색되지 않는다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedSeoul() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "서울")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim2.getId())); // 날짜순 정렬

    }


    // 3차 테스트 - 검색어 '강남' - 검색 결과 : moim3 (제목), moim1 (지역) - 검색어로 매핑된 2차 지역 (구/읍) 은 검색된다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedGangNam() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "강남")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim3.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim1.getId()));

    }


    // 4차 테스트 - 검색어 '강남' + 지역 필터 '강북구' - 검색 결과 : moim3 (제목, 지역카테고리) - moim1 는 지역이 강남구이므로 검색에서 제외된다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedGangNamWithAreaFilterGangNam() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "강남")
                .param("areaFilter", CITY_GANGNAM.getName())
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim1.getId())); // 날짜순 정렬

    }


    // 5차 테스트 - 검색어 '우리' + 지역 필터 '서울시 전체' - 검색 결과 : moim4 (제목), moim9 (제목) - 서울시 전체 필터이므로 둘 다 적합하다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedWooRiWithAreaFilterSeoul() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "우리")
                .param("areaFilter", STATE_SEOUL.getName())
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim9.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim4.getId()));

    }


    // 6차 테스트 - 검색어 '반려동물' - 검색 결과 : moim4 (제목) - 'PET' 카테고리는 1차 카테고리이므로 moim6이 검색되지 않는다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPet() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "반려동물")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim4.getId()));

    }


    // 7차 테스트 - 검색어 '강아지' - 검색 결과 : moim3 (제목), moim6 (카테고리), moim10 (카테고리) - 'DOG' 카테고리는 2차 카테고리이므로, 모두 검색된다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedDog() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "강아지")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim10.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim3.getId()));

    }


    // 8차 테스트 - 검색어 '강아지' + 카테고리 필터 '강아지' - 검색 결과 : moim6 (이름 + 카테고리) - moim3 의 카테고리는 DOG 가 아니므로 제외되고, moim10 의 이름에는 강아지가 포함되어 있지 않아 제외됨
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedDogWithCategoryFilterDog() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "강아지")
                .param("categoryFilter", "강아지")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim6.getId()));


    }


    // 9차 테스트 - 검색어 '카메라' + 카테고리 필터 '강아지' - 검색 결과 : moim6 (제목 + 카테고리) - 제목 검색에서 moim10 은 제외된다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedCameraWithCategoryFilterDog() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "카메라")
                .param("categoryFilter", "강아지")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim6.getId())); // 날짜순 정렬

    }


    // 10차 테스트 - 검색어 '강아지' + 카테고리 필터 '클라이밍' - 검색 결과 : moim3 (제목 + 카테고리) - moim6, 10 은 카테고리에서 제외된다
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedDogWithCategoryFilterClimbing() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "강아지")
                .param("categoryFilter", "클라이밍")
                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim3.getId())); // 날짜순 정렬

    }


    // 11차 테스트 - 검색어 '사람' - 검색 결과 : moim2 (제목), moim6 (제목), moim9 (제목), moim10 (제목)
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPerson() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(4)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim10.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim9.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim2.getId()));

    }


    // 12차 테스트 - 검색어 '사람' offset1 + limit2 - 검색 결과 : moim6, moim9 (1항부터 두개) (offset 이 1 이길 희망하면 그 전엔 moim10 이 제일 마지막 건, 위 Test Base)
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithOffset1Limit2() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
                .param("lastMoimId", moim10.getId().toString())
//                .param("offset", "1")
                .param("limit", "2")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim9.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim6.getId()));

    }


    // 13차 테스트 - 검색어 '사람' offset2 + limit1 - 검색 결과 : moim6 // 10, 9, 6, 2 중 두번째 항에서 한 개, 그럼 9가 마지막 모임
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithOffset2Limit1() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
//                .param("offset", "2")
                .param("lastMoimId", moim9.getId().toString())
                .param("limit", "1")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim6.getId()));

    }


    // 14차 테스트 - 검색어 '사람' offset1 + limit10 - 검색 결과 : moim2, moim6, moim9 // 10, 9 , 6, 2 중 9부터 이므로 10이 마지막
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithOffset1Limit10() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
//                .param("offset", "1")
                .param("limit", "10")
                .param("lastMoimId", moim10.getId().toString())
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(3)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim9.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim2.getId()));

    }


    // 14차 - 1 테스트 (위 상황에서 필터도 낌) - 검색어 '사람' offset1 + limit10 - 검색 결과 : moim2, moim6, moim9 // 10, 9 , 6, 2 중 9부터 이므로 10이 마지막
    //                      10,9,6,2 중 지역 필터도 끼면, AND 조건이 만족된 이후 limit 을 건다는 점. 따라서 offset 조건은 지금 달라짐
    //
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithOffset1Limit10WithFilter() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // given - data change - 9, 6, 2 를 지역을 노원으로 바꿔보자 그럼 필터 때문에 사람으로 검색해도 9,6,2 만 검색된다
        moim9 = em.find(Moim.class, moim9.getId());
        moim6 = em.find(Moim.class, moim6.getId());
        moim2 = em.find(Moim.class, moim2.getId());

        MoimReqDto.MoimUpdateReqDto updater = new MoimReqDto.MoimUpdateReqDto();
        updater.setAreaCity("노원구");
        moim9.updateMoim(updater, new ArrayList<>(), null);
        moim6.updateMoim(updater, new ArrayList<>(), null);
        moim2.updateMoim(updater, new ArrayList<>(), null);

        em.flush();
        em.clear();


        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
                .param("areaFilter", "노원구") // 그럼 9,6,2 가 검색되는 중,  이 때 9를 마지막 검색으로 잡는다면 6,2가 검색될 것
                .param("limit", "10")
                .param("lastMoimId", moim9.getId().toString())
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
//        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim9.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim6.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim2.getId()));

    }


    // 15차 테스트 - 검색어 '사람' + 카테고리 필터 '강아지' + 지역 필터 '서울시 전체' - 검색 결과 : moim10, moim 6
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithAreaFilterSeoulCategoryFilterDog() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
                .param("areaFilter", "서울시")
                .param("categoryFilter", "강아지")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim10.getId())); // 날짜순 정렬
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim6.getId()));

    }


    // 16차 테스트 - 검색어 '사람' + 카테고리 필터 '강아지' + 지역 필터 '도봉구' - 검색 결과 : moim10 (ALL MATCH)
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithAreaFilterDoBongCategoryFilterDog() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
                .param("areaFilter", "도봉구")
                .param("categoryFilter", "강아지")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim10.getId())); // 날짜순 정렬

    }


    // 17차 테스트 - 검색어 '사람' + 카테고리 필터 '스터디' + 지역 필터 '노원구' - 검색 결과 : moim9 (ALL MATCH)
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithAreaFilterNoWonCategoryFilterStudy() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
                .param("areaFilter", "노원구")
                .param("categoryFilter", "스터디")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(1)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim9.getId())); // 날짜순 정렬

    }


    // 18차 테스트 - 검색어 '사람' + 카테고리 필터 '스터디' + 지역 필터 '도봉구' - 검색 결과 : 없음
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedPersonWithAreaFilterDoBongCategoryFilterStudy() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "사람")
                .param("areaFilter", "도봉구")
                .param("categoryFilter", "스터디")
//                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(0)));

    }


    void suData() {

        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, role);
        member2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, role);
        member3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, role);
        member4 = makeTestMember(memberEmail4, memberPhone4, memberName4, nickname4, ci4, role);
        member5 = makeTestMember(memberEmail5, memberPhone5, memberName5, nickname5, ci5, role);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);

        Category testCategory1 = new Category(1L, fromValue(DANCE.getValue()), 0, null);
        Category testCategory1_1 = new Category(2L, fromValue(LATIN_DANCE.getValue()), 1, testCategory1);
        Category testCategory2 = new Category(3L, fromValue(OUTDOOR.getValue()), 0, null);
        Category testCategory2_1 = new Category(4L, fromValue(INTERNATIONAL.getValue()), 1, testCategory2);
        Category testCategory3 = new Category(5L, fromValue(EXERCISE.getValue()), 0, null);
        Category testCategory3_1 = new Category(6L, fromValue(CLIMBING.getValue()), 1, testCategory3);
        Category testCategory4 = new Category(7L, fromValue(JOB.getValue()), 0, null);
        Category testCategory4_1 = new Category(8L, fromValue(STUDY.getValue()), 1, testCategory4);
        Category testCategory5 = new Category(9L, fromValue(CRAFTS.getValue()), 0, null);
        Category testCategory5_1 = new Category(10L, fromValue(LEATHER.getValue()), 1, testCategory5);
        Category testCategory6 = new Category(11L, fromValue(PET.getValue()), 0, null);
        Category testCategory6_1 = new Category(12L, fromValue(DOG.getValue()), 1, testCategory6);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        em.persist(testCategory2);
        em.persist(testCategory2_1);
        em.persist(testCategory3);
        em.persist(testCategory3_1);
        em.persist(testCategory4);
        em.persist(testCategory4_1);
        em.persist(testCategory5);
        em.persist(testCategory5_1);
        em.persist(testCategory6);
        em.persist(testCategory6_1);


        List<Category> moim1Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim2Category = List.of(testCategory2, testCategory2_1);
        List<Category> moim3Category = List.of(testCategory3, testCategory3_1);
        List<Category> moim4Category = List.of(testCategory4, testCategory4_1);
        List<Category> moim5Category = List.of(testCategory5, testCategory5_1);
        List<Category> moim6Category = List.of(testCategory6, testCategory6_1);
        List<Category> moim7Category = List.of(testCategory1, testCategory1_1);
        List<Category> moim8Category = List.of(testCategory2, testCategory2_1);
        List<Category> moim9Category = List.of(testCategory4, testCategory4_1);
        List<Category> moim10Category = List.of(testCategory6, testCategory6_1);


        moim1 = makeTestMoim(moim1Name, 10, moim1Area.getState(), moim1Area.getCity(), moim1Category, member1);
        moim2 = makeTestMoim(moim2Name, 10, moim2Area.getState(), moim2Area.getCity(), moim2Category, member2);
        moim3 = makeTestMoim(moim3Name, 10, moim3Area.getState(), moim3Area.getCity(), moim3Category, member3);
        moim4 = makeTestMoim(moim4Name, 10, moim4Area.getState(), moim4Area.getCity(), moim4Category, member4);
        moim5 = makeTestMoim(moim5Name, 10, moim5Area.getState(), moim5Area.getCity(), moim5Category, member5);
        moim6 = makeTestMoim(moim6Name, 10, moim6Area.getState(), moim6Area.getCity(), moim6Category, member1);
        moim7 = makeTestMoim(moim7Name, 10, moim7Area.getState(), moim7Area.getCity(), moim7Category, member2);
        moim8 = makeTestMoim(moim8Name, 10, moim8Area.getState(), moim8Area.getCity(), moim8Category, member3);
        moim9 = makeTestMoim(moim9Name, 10, moim9Area.getState(), moim9Area.getCity(), moim9Category, member4);
        moim10 = makeTestMoim(moim10Name, 10, moim10Area.getState(), moim10Area.getCity(), moim10Category, member5);

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


        MoimJoinRule joinRule1 = makeTestMoimJoinRule(true, 50, 25, MemberGender.N);
        MoimJoinRule joinRule2 = makeTestMoimJoinRule(true, 40, 20, MemberGender.F);
        MoimJoinRule joinRule3 = makeTestMoimJoinRule(true, 25, 18, MemberGender.N);
        em.persist(joinRule1);
        em.persist(joinRule2);
        em.persist(joinRule3);

        moim1.setMoimJoinRule(joinRule1);
        moim2.setMoimJoinRule(joinRule2);
        moim4.setMoimJoinRule(joinRule2);
        moim7.setMoimJoinRule(joinRule3);
        moim8.setMoimJoinRule(joinRule3);

        em.flush();
        em.clear();
    }

}
