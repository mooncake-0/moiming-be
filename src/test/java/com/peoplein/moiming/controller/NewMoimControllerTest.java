package com.peoplein.moiming.controller;


import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.domain.enums.AreaValue.*;
import static com.peoplein.moiming.domain.enums.CategoryName.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestParams.*;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class NewMoimControllerTest extends TestObjectCreator {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member1, member2, member3;
    private Moim moim1, moim2, moim3, moim4, moim5;


    private String moim1Name = "라틴어를 강남에서 배우는 공간";
    private String moim2Name = "서울사는 사람들";
    private String moim3Name = "강아지들 모여라";
    private String moim4Name = "우리집 반려동물";
    private String moim5Name = "프로그래밍 스터디";


    private Area moim1Area = new Area(STATE_SEOUL.getName(), CITY_GANGNAM.getName());
    private Area moim2Area = new Area(STATE_SEOUL.getName(), CITY_GANGDONG.getName());
    private Area moim3Area = new Area(STATE_SEOUL.getName(), CITY_GANGNAM.getName());
    private Area moim4Area = new Area(STATE_SEOUL.getName(), CITY_GANGDONG.getName());
    private Area moim5Area = new Area(STATE_SEOUL.getName(), CITY_GANGNAM.getName());

    // 실패 - offset 다른 값으로 조절
    @Test
    void getNewMoims_shouldReturn400_whenOtherOffsetTrial_byMoimingApiException() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(AppUrlPath.PATH_MOIM_NEW)
                .header(HEADER, PREFIX + accessToken)
                .param("offset", "2"));

        // then
        resultActions.andExpect(status().isBadRequest());

    }


    // 성공 - 최신 모임들 조회 성공
    @Test
    void getNewMoims_shouldReturn200_whenNormalTrial() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(AppUrlPath.PATH_MOIM_NEW)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(5)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim5.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim1.getId()));

    }


    // 성공 - limit 조절2
    @Test
    void getNewMoims_shouldReturn200_whenNormalTrialWithLimit() throws Exception {

        // given
        suData();
        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(AppUrlPath.PATH_MOIM_NEW)
                .header(HEADER, PREFIX + accessToken)
                .param("limit", "2"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim4.getId()));
//        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim2.getId()));
//        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim5.getId()));
//        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim1.getId()));

    }


    void suData() {

        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        member2 = makeTestMember(memberPhone2, nickname2, ci2, role);
        member3 = makeTestMember(memberPhone3, nickname3, ci3, role);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);

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

        moim1 = makeTestMoim(moim1Name, 10, moim1Area.getState(), moim1Area.getCity(), moim1Category, member1);
        moim2 = makeTestMoim(moim2Name, 10, moim2Area.getState(), moim2Area.getCity(), moim2Category, member2);
        moim3 = makeTestMoim(moim3Name, 10, moim3Area.getState(), moim3Area.getCity(), moim3Category, member3);
        moim4 = makeTestMoim(moim4Name, 10, moim4Area.getState(), moim4Area.getCity(), moim4Category, member1);
        moim5 = makeTestMoim(moim5Name, 10, moim5Area.getState(), moim5Area.getCity(), moim5Category, member3);

        MoimJoinRule joinRule1 = makeTestMoimJoinRule(true, 50, 25, MemberGender.N);
        MoimJoinRule joinRule2 = makeTestMoimJoinRule(true, 40, 20, MemberGender.F);
        MoimJoinRule joinRule3 = makeTestMoimJoinRule(true, 25, 18, MemberGender.N);

        moim1.setMoimJoinRule(joinRule1);
        moim2.setMoimJoinRule(joinRule2);
        moim4.setMoimJoinRule(joinRule2);

        // FLUSH 를 통해 생성 순서 지정 (1>5>2>4>3
        em.persist(moim1);
        em.persist(joinRule1);
        em.flush();

        em.persist(moim5);
        em.flush();

        em.persist(moim2);
        em.persist(joinRule2);
        em.flush();

        em.persist(moim4);
        em.flush();

        em.persist(moim3);
        em.persist(joinRule3);

        em.flush();
        em.clear();
    }
}
