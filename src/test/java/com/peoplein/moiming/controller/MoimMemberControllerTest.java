package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestModelParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.MoimMemberRoleType.*;
import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.support.TestModelParams.moimArea;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimMemberControllerTest extends TestObjectCreator {

    public final String MOIM_MEMBER_BASE_URL = API_SERVER + API_MOIM_VER + API_MOIM_MEMBER;
    public final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    // 실제 간이 저장 필요, Mock 이 아닌 Autowire 필요
    @Autowired
    private EntityManager em;

    @Autowired
    private MoimMemberRepository moimMemberRepository;


    private Member testMember1; // Moim1 모임장
    private Member testMember2; // Moim1 모임원  Moim2 모임장
    private Member testMember3; // Moim1 미가입  Moim3 미가입
    private Member testMember4; // Moim1 스스나감 Moim2 강퇴당함
    private Moim testMoim1;


    @BeforeEach
    void be() { // 상황 SU 하기

        Role testRole = makeTestRole(RoleType.USER);
        em.persist(testRole);

        testMember1 = makeTestMember(memberEmail, memberPhone, memberName, nickname, testRole);
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2,testRole);
        testMember3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, testRole);
        testMember4 = makeTestMember(memberEmail4, memberPhone4, memberName4, nickname4, testRole);
        em.persist(testMember1);
        em.persist(testMember2);
        em.persist(testMember3);
        em.persist(testMember4);

        // 모임 카테고리 사전생성
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 2, testCategory1);
        Category testCategory2 = new Category(3L, CategoryName.fromValue(depth1SampleCategory2), 1, null);
        Category testCategory2_1 = new Category(4L, CategoryName.fromValue(depth2SampleCategory2), 2, testCategory2);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        em.persist(testCategory2);
        em.persist(testCategory2_1);

        testMoim1 = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), testMember1);
        em.persist(testMoim1);

        MoimMember.memberJoinMoim(testMember2, testMoim1, NORMAL, ACTIVE);

        em.flush();
        em.clear();
    }

    // TEST 대상


    // getActiveMoimMembers() - 모임의 모든 회원 및 상태 조회
    // 모임원이 요청하면 잘 된다
    @Test
    void getActiveMoimMembers_shouldReturn200_whenMoimMemberRequests() throws Exception {

        // given
        Long moimId = testMoim1.getId();
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(MOIM_MEMBER_BASE_URL + "/" + moimId)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());

    }


    // 모임원이 아닌 유저가 요청해도 잘 된다 TODO :: 이건 현재 요구사항 확정 필요
    @Test
    void getActiveMoimMembers_shouldReturn200_whenNotMoimMemberRequests() throws Exception {

        // given
        Long moimId = testMoim1.getId();
        String testAccessToken = createTestJwtToken(testMember3, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(MOIM_MEMBER_BASE_URL + "/" + moimId)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());

    }

    // 성공 CASE - 잘 가져오는지, size, 모두 다 ACTIVE 인지 등을 확인
    // 실패 CASE 작성
    // CASE1 moimId 안들어옴
    // CASE2 잘못된 moim Id 전달

    // joinMoim() - 모임 가입하기
    // 강퇴가 join 시도
    // 휴면복구 유저가 join 시도 (IBD 상태)
    // 스스로 나감이 join 시도
    // 실패 CASE 작성

    // leaveMoim() - 모임 나가기
    // 실패 CASE 작성

    // expelMember() - 모임원 강퇴하기
    // 실패 CASE 작성

}
