package com.peoplein.moiming.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.MoimMemberRoleType.*;
import static com.peoplein.moiming.domain.enums.MoimMemberState.ACTIVE;
import static com.peoplein.moiming.domain.enums.MoimMemberState.IBW;
import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.HEADER;
import static com.peoplein.moiming.security.token.JwtParams.PREFIX;
import static com.peoplein.moiming.support.TestParams.*;
import static com.peoplein.moiming.support.TestParams.moimArea;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional // Test Data 저장을 위함
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ManagerDelegationIntegrationTest extends TestObjectCreator {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc mvc;

    private Member testMember1, testMember2, testMember3, testMember4;
    private Moim testMoim;
    private List<Category> testMoimCategories;

    void dataSu() { // 상황 SU 하기

        Role testRole = makeTestRole(RoleType.USER);
        em.persist(testRole);

        testMember1 = makeTestMember(memberPhone, nickname, ci, testRole);
        testMember2 = makeTestMember(memberPhone2, nickname2, ci2, testRole);
        testMember3 = makeTestMember(memberPhone3, nickname3, ci3, testRole);
        testMember4 = makeTestMember(memberPhone4, nickname4, ci4, testRole);
        em.persist(testMember1);
        em.persist(testMember2);
        em.persist(testMember3);
        em.persist(testMember4);

        insertTestMemberInfo(testMember1, MemberGender.N, LocalDate.now());
        insertTestMemberInfo(testMember2, MemberGender.N, LocalDate.now());
        insertTestMemberInfo(testMember3, MemberGender.N, LocalDate.now());
        insertTestMemberInfo(testMember4, MemberGender.N, LocalDate.now());

        // 모임 카테고리 사전생성
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        testMoimCategories = List.of(testCategory1, testCategory1_1);

        testMoim = makeTestMoim(moimName, maxMember, moimArea, testMoimCategories, testMember1, -1, -1, MemberGender.N);
        em.persist(testMoim);

        MoimMember.memberJoinMoim(testMember3, testMoim, NORMAL, ACTIVE);
        em.flush();

        MoimMember.memberJoinMoim(testMember4, testMoim, NORMAL, ACTIVE);
        em.flush();

        MoimMember.memberJoinMoim(testMember2, testMoim, NORMAL, ACTIVE);
        em.flush();
        em.clear();

    }


    void insertTestMemberInfo(Member member, MemberGender memberGender, LocalDate memberBirth) {
        // given - 개인정보 기입완료
        MemberInfo curMemberInfo = makeMemberInfo(member, memberGender, memberBirth);
        em.persist(curMemberInfo);
    }


    // 상황 설계
    // 모임장이 모임을 나가는데, 남은 모임원이 없음
    @Test
    void leaveMoim_shouldSuccessAndMarkDeletion_whenReqByManagerAndNoMemberLeft() throws Exception {

        // given
        dataSu();
        Moim deleteTargetMoim = makeTestMoim(moimName, maxMember, moimArea, testMoimCategories, testMember1, -1, -1, MemberGender.N);
        em.persist(deleteTargetMoim);
        em.flush();
        em.clear();

        // given
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto(deleteTargetMoim.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        em.flush();
        em.clear();

        // then - db verify
        Moim deletedMoim = em.find(Moim.class, deleteTargetMoim.getId());
        assertNotNull(deletedMoim.getDeletedAt());
        assertThat(deletedMoim.getCurMemberCount()).isEqualTo(0);
        assertThat(deletedMoim.getCurManagerId()).isEqualTo(testMember1.getId());

    }


    // 모임원1,2,3,4 가입 (가입순서 1->3->4->2)
    // 모임장이 모임을 나감, 모임장 승계 여부 확인 (모임원 수, 1의 상태, 3의 상태 확인)
    @Test
    void leaveMoim_shouldSuccessAndDelegateManager_whenReqByManagerAndMemberExists() throws Exception {

        // given
        dataSu();
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto(testMoim.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        em.flush();
        em.clear();

        // then - db verify
        Moim tgMoim = em.find(Moim.class, testMoim.getId());
        assertNull(tgMoim.getDeletedAt());
        assertThat(tgMoim.getCurMemberCount()).isEqualTo(3);
        assertThat(tgMoim.getCurManagerId()).isEqualTo(testMember3.getId());
        assertThat(tgMoim.getCreatorId()).isEqualTo(testMember1.getId());

        MoimMember moimMember1 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember1.getId())
                .getSingleResult();
        assertThat(moimMember1.getMemberState()).isEqualTo(IBW);
        assertThat(moimMember1.getMemberRoleType()).isEqualTo(MANAGER);

        MoimMember moimMember3 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember3.getId())
                .getSingleResult();
        assertThat(moimMember3.getMemberState()).isEqualTo(ACTIVE);
        assertThat(moimMember3.getMemberRoleType()).isEqualTo(MANAGER);

    }


    // 위 상황 이후 1이 재가입 함 -> 모임원 수, 1의 상태, 3의 상태 확인 (재가입 시 created_at 도 확인)
    @Test
    void joinMoim_shouldSuccess_whenFormerManagerReq() throws Exception {

        // given - 유저 1 모임장이 나갔던 상황
        dataSu();
        MoimMemberLeaveReqDto rd = new MoimMemberLeaveReqDto(testMoim.getId());
        String rb = om.writeValueAsString(rd);
        String tat = createTestJwtToken(testMember1, 2000);
        ResultActions ra = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(rb).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + tat));
        em.flush();
        em.clear();

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        em.flush();
        em.clear();

        // then - db verify
        Moim tgMoim = em.find(Moim.class, testMoim.getId());
        assertNull(tgMoim.getDeletedAt());
        assertThat(tgMoim.getCurMemberCount()).isEqualTo(4);
        assertThat(tgMoim.getCurManagerId()).isEqualTo(testMember3.getId());
        assertThat(tgMoim.getCreatorId()).isEqualTo(testMember1.getId());

        MoimMember moimMember1 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember1.getId())
                .getSingleResult();
        assertThat(moimMember1.getMemberState()).isEqualTo(ACTIVE);
        assertThat(moimMember1.getMemberRoleType()).isEqualTo(FORMER_MANAGER);

        MoimMember moimMember3 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember3.getId())
                .getSingleResult();
        assertThat(moimMember3.getMemberState()).isEqualTo(ACTIVE);
        assertThat(moimMember3.getMemberRoleType()).isEqualTo(MANAGER);

        // 이전 MANAGER 였어도 재가입시 created_at RESET 확인
        assertTrue(moimMember3.getCreatedAt().isBefore(moimMember1.getCreatedAt()));

    }


    // 위 상황 이후 모임장이 나감 -> 1의 상태, 3의 상태, 4의 상태 확인 (4가 모임장이 되는 모습 확인)
    @Test
    void leaveMoim_shouldSuccessAndDelegateManager_whenSecondManagerReq() throws Exception {

        // given - 유저 1 모임장이 나갔던 상황과, 유저 1이 다시 들어온 상황을 포함한 이후 상황이다
        dataSu();
        MoimMemberLeaveReqDto rd = new MoimMemberLeaveReqDto(testMoim.getId());
        String rb = om.writeValueAsString(rd);
        String tat = createTestJwtToken(testMember1, 2000);
        ResultActions ra = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(rb).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + tat));
        em.flush();
        em.clear();
        MoimMemberJoinReqDto rd2 = new MoimMemberJoinReqDto(testMoim.getId());
        String rb2 = om.writeValueAsString(rd2);
        String tat2 = createTestJwtToken(testMember1, 2000);
        ResultActions ra2 = mvc.perform(post(PATH_MOIM_MEMBER_JOIN)
                .content(rb2).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + tat2));
        em.flush();
        em.clear();

        // given
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto(testMoim.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember3, 2000);
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        em.flush();
        em.clear();

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        Moim tgMoim = em.find(Moim.class, testMoim.getId());
        assertNull(tgMoim.getDeletedAt());
        assertThat(tgMoim.getCurMemberCount()).isEqualTo(3);
        assertThat(tgMoim.getCurManagerId()).isEqualTo(testMember4.getId());
        assertThat(tgMoim.getCreatorId()).isEqualTo(testMember1.getId());

        MoimMember moimMember1 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember1.getId())
                .getSingleResult();
        assertThat(moimMember1.getMemberState()).isEqualTo(ACTIVE);
        assertThat(moimMember1.getMemberRoleType()).isEqualTo(FORMER_MANAGER);

        MoimMember moimMember3 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember3.getId())
                .getSingleResult();
        assertThat(moimMember3.getMemberState()).isEqualTo(IBW);
        assertThat(moimMember3.getMemberRoleType()).isEqualTo(MANAGER);

        MoimMember moimMember4 = em.createQuery("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.member.id = :memberId", MoimMember.class)
                .setParameter("moimId", testMoim.getId())
                .setParameter("memberId", testMember4.getId())
                .getSingleResult();
        assertThat(moimMember4.getMemberState()).isEqualTo(ACTIVE);
        assertThat(moimMember4.getMemberRoleType()).isEqualTo(MANAGER);

    }

}
