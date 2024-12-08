package com.peoplein.moiming.controller;


import com.peoplein.moiming.config.AppUrlPath;
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

import java.time.LocalDate;
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


    private Area moim1Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim2Area = new Area(STATE_SEOUL, CITY_GANGDONG);
    private Area moim3Area = new Area(STATE_SEOUL, CITY_GANGNAM);
    private Area moim4Area = new Area(STATE_SEOUL, CITY_GANGDONG);
    private Area moim5Area = new Area(STATE_SEOUL, CITY_GANGNAM);

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

    // 성공 - 최신 모임들 조회 성공
    // 단, 가장 인기 높은 모임 3가지 추가 - 삭제된 모임, 비활성화된 모임, 정상 모임 - 2가지는 조회 안되고, 1가지 정상 조회됨을 확인
    @Test
    void getNewMoims_shouldReturn200_whenNormalAndDeletedAndHiddenPostExists() throws Exception {

        // given
        suData();

        // 비활성화된 모임들 추가
        Category tmpCategory1 = new Category(1L, fromValue(DANCE.getValue()), 0, null);
        Category tmpCategory2 = new Category(2L, fromValue(LATIN_DANCE.getValue()), 1, tmpCategory1);
        List<Category> tmpCategories = List.of(tmpCategory1, tmpCategory2);

        Moim deletedMoim = makeTestMoim("가장 최근에 생성된 삭제된 모임", 10, moim1Area, tmpCategories, member1, 1974, 2004, MemberGender.N); //1
        deletedMoim.minusCurMemberCount(); // 한 명나가서, 삭제라고 인식하도록 만듬
        em.persist(deletedMoim);
        em.flush();
        Thread.sleep(100);

        Moim hiddenMoim = makeTestMoim("가장 최근에 생성된 신고 숨김처리된 모임", 10, moim1Area, tmpCategories, member1, 1974, 2004, MemberGender.N); //1
        hiddenMoim.changeContentStatus(ContentStatus.HIDDEN, 1000L);
        em.persist(hiddenMoim);
        em.flush();
        Thread.sleep(100);

        // 비활성화 때문에 제외되는게 맞는지 확인하기 위해 정상 추가
        Moim normalMoim = makeTestMoim("가장 최근에 생성된 정상 모임", 10, moim1Area, tmpCategories, member1, 1974, 2004, MemberGender.N); //1
        em.persist(normalMoim);

        em.flush();
        em.clear();

        String accessToken = createTestJwtToken(member1, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(AppUrlPath.PATH_MOIM_NEW)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(6))); // 정상만 추가된 채로 조회된다
        resultActions.andExpect(jsonPath("$.data[0].moimId").value(normalMoim.getId())); // 제일 최근 정상 모임
        resultActions.andExpect(jsonPath("$.data[1].moimId").value(moim3.getId()));
        resultActions.andExpect(jsonPath("$.data[2].moimId").value(moim4.getId()));
        resultActions.andExpect(jsonPath("$.data[3].moimId").value(moim2.getId()));
        resultActions.andExpect(jsonPath("$.data[4].moimId").value(moim5.getId()));
        resultActions.andExpect(jsonPath("$.data[5].moimId").value(moim1.getId()));

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


    void suData() throws Exception {

        Role role = makeTestRole(RoleType.USER);
        em.persist(role);

        member1 = makeTestMember(memberPhone, nickname, ci, role);
        member2 = makeTestMember(memberPhone2, nickname2, ci2, role);
        member3 = makeTestMember(memberPhone3, nickname3, ci3, role);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);

        // 가입조건에 부합하게 생성될 수 있도록 지정
        MemberInfo member1Info = makeMemberInfo(member1, memberGender, memberBirth);
        em.persist(member1Info);
        MemberInfo member2Info = makeMemberInfo(member2, MemberGender.F, LocalDate.of(2000, 1,1));
        em.persist(member2Info);
        MemberInfo member3Info = makeMemberInfo(member3, memberGender, memberBirth);
        em.persist(member3Info);

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

        moim1 = makeTestMoim(moim1Name, 10, moim1Area, moim1Category, member1, 1974, 1999, MemberGender.N);
        moim2 = makeTestMoim(moim2Name, 10, moim2Area, moim2Category, member2, 1984, 2004, MemberGender.F);
        moim3 = makeTestMoim(moim3Name, 10, moim3Area, moim3Category, member3, -1, -1, MemberGender.N);
        moim4 = makeTestMoim(moim4Name, 10, moim4Area, moim4Category, member1, 1984, 2004, MemberGender.N);
        moim5 = makeTestMoim(moim5Name, 10, moim5Area, moim5Category, member3, -1, -1, MemberGender.N);


        // FLUSH 를 통해 생성 순서 지정 (1>5>2>4>3
        em.persist(moim1);
        em.flush();
        Thread.sleep(100);

        em.persist(moim5);
        em.flush();
        Thread.sleep(100);

        em.persist(moim2);
        em.flush();
        Thread.sleep(100);

        em.persist(moim4);
        em.flush();
        Thread.sleep(100);

        em.persist(moim3);
        em.flush();
        em.clear();
    }
}
