package com.peoplein.moiming.controller;


import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.repository.SearchJpaRepository;
import com.peoplein.moiming.security.token.JwtParams;
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
import static com.peoplein.moiming.domain.enums.MoimSearchType.NO_FILTER;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@Rollback(value = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SearchControllerTest extends TestObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SearchJpaRepository searchRepository;

    @Autowired
    private EntityManager em;

    private Member member1, member2, member3, member4, member5;
    private Moim moim1, moim2, moim3, moim4, moim5, moim6, moim7, moim8, moim9, moim10;

    private String moim1Name = "라틴어를 배우는 공간";
    private String moim2Name = "서울사는 사람들";
    private String moim3Name = "강남 강아지들 모여라";
    private String moim4Name = "우리집 반려동물";
    private String moim5Name = "프로그래밍 스터디";
    private String moim6Name = "카메라 찍는 사람들";
    private String moim7Name = "여행 여기저기 다녀보자";
    private String moim8Name = "적합한 직무 찾기";
    private String moim9Name = "우리는 누굴 위해 사는가";
    private String moim10Name = "집에 너무 가고 싶은 사람들";

    private Area moim1Area = new Area(STATE_SEOUL.getName(), CITY_GANGNAM.getName());
    private Area moim2Area = new Area(STATE_SEOUL.getName(), CITY_GANGDONG.getName());
    private Area moim3Area = new Area(STATE_SEOUL.getName(), CITY_GANGBUK.getName());
    private Area moim4Area = new Area(STATE_SEOUL.getName(), CITY_GANGSEO.getName());
    private Area moim5Area = new Area(STATE_SEOUL.getName(), CITY_GWANAK.getName());
    private Area moim6Area = new Area(STATE_SEOUL.getName(), CITY_GWANGJIN.getName());
    private Area moim7Area = new Area(STATE_SEOUL.getName(), CITY_GURO.getName());
    private Area moim8Area = new Area(STATE_SEOUL.getName(), CITY_GEUMCHEON.getName());
    private Area moim9Area = new Area(STATE_SEOUL.getName(), CITY_NOWON.getName());
    private Area moim10Area = new Area(STATE_SEOUL.getName(), CITY_DOBONG.getName());

    // 실패 테스트


    // 1차 테스트 - 검색어 '라틴' - 검색 결과 : moim1 (제목), moim7 (카테고리)
    @Test
    void searchMoim_shoulReturnSearchedMoim_whenSearchedLatin() throws Exception{

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_SEARCH_MOIM)
                .param("keyword", "라틴")
                .param("offset", "0")
                .header(HEADER, PREFIX + accessToken));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());

    }


    // 2차 테스트 - 검색어

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


        MoimJoinRule joinRule1 = makeTestMoimJoinRule(true, 50,25,  MemberGender.N);
        MoimJoinRule joinRule2 = makeTestMoimJoinRule(true, 40,20, MemberGender.F);
        MoimJoinRule joinRule3 = makeTestMoimJoinRule(true, 25, 18,  MemberGender.N);
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
