package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.repository.MoimMemberRepository;
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
import java.util.Optional;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.MoimMemberRoleType.*;
import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.support.TestModelParams.moimArea;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimMemberControllerTest extends TestObjectCreator {


    public final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    // 실제 간이 저장 필요, Mock 이 아닌 Autowire 필요
    @Autowired
    private EntityManager em;

    @Autowired
    private MoimMemberRepository moimMemberRepository;

    private Member testMember1; // Moim1 모임장
    private Member testMember2; // Moim1 모임원
    private Member testMember3; // Moim1 미가입
    private Member testMember; // 각종 Test 에 상황에 따라 적용될 멤버
    private Moim testMoim1;
    private List<Category> testMoimCategories;

    @BeforeEach
    void be() { // 상황 SU 하기

        Role testRole = makeTestRole(RoleType.USER);
        em.persist(testRole);

        testMember1 = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        testMember3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        testMember = makeTestMember(memberEmail4, memberPhone4, memberName4, nickname4, ci4, testRole);
        em.persist(testMember1);
        em.persist(testMember2);
        em.persist(testMember3);
        em.persist(testMember);

        // 모임 카테고리 사전생성
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        testMoimCategories = List.of(testCategory1, testCategory1_1);

        testMoim1 = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), testMoimCategories, testMember1);
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
        Long requestMemberId = testMember2.getId();
        Long moimId = testMoim1.getId();
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        String[] params = {"moimId"};
        String[] vals = {moimId + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_MEMBER_GET_VIEW, params, vals))
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2)));
        resultActions.andExpect(jsonPath("$.data[?(@.memberRoleType=='MANAGER')]").exists());
        resultActions.andExpect(jsonPath("$.data[*].memberState", everyItem(is(ACTIVE.name())))); // 모두 ACTIVE 한 멤버들이여야 한다

        // then - 나도 거기에 포함되어 있음
        String jsonPathExpression = String.format("$.data[?(@.memberDto.memberId==%d)]", requestMemberId);
        resultActions.andExpect(jsonPath(jsonPathExpression).exists());

    }


    // 모임원이 아닌 유저가 요청해도 잘 된다 TODO :: 이건 현재 요구사항 확정 필요
    @Test
    void getActiveMoimMembers_shouldReturn200_whenNotMoimMemberRequests() throws Exception {

        // given
        Long moimId = testMoim1.getId();
        Long requestMemberId = testMember3.getId();
        String testAccessToken = createTestJwtToken(testMember3, 2000);

        String[] params = {"moimId"};
        String[] vals = {moimId + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_MEMBER_GET_VIEW, params, vals))
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray());
        resultActions.andExpect(jsonPath("$.data", hasSize(2))); // 나는 없고 두명이 가입되어 있음
        resultActions.andExpect(jsonPath("$.data[?(@.memberRoleType=='MANAGER')]").exists());
        resultActions.andExpect(jsonPath("$.data[*].memberState", everyItem(is(ACTIVE.name()))));

        // then - 나는 거기에 없어야 한다
        String jsonPathExpression = String.format("$.data[?(@.memberDto.memberId==%d)]", requestMemberId);
        resultActions.andExpect(jsonPath(jsonPathExpression).doesNotExist());

    }


    // 실패 CASE 작성
    // CASE1 moimId 안들어옴 // 에러 난다
    @Test
    void getActiveMoimMember_shouldReturn404_whenMoimIdNotPassed() throws Exception {

        // given
        String testAccessToken = createTestJwtToken(testMember2, 2000);
        String[] params = {"moimId"};
        String[] vals = {" "};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_MEMBER_GET_VIEW, params, vals)) // NULL 로 전달시 에러
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isInternalServerError());
    }


    // CASE2 잘못된 moim Id 전달
    @Test
    void getActiveMoimMember_shouldReturn404_whenMoimNotFound_byMoimingApiException() throws Exception {

        // given
        Long moimId = 1000L;
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        String[] params = {"moimId"};
        String[] vals = {moimId + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_MEMBER_GET_VIEW, params, vals))
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_NOT_FOUND.getErrCode()));
    }


    // joinMoim() - 모임 가입하기
    // 성공 CASE
    // 일반 유저가 join 시도 (MoimMember 가 한번도 없었음)
    @Test
    void joinMoim_shouldReturn200_whenFirstMemberJoinSuccessful() throws Exception { // Moim1 에 Member3 가 가입요청 진행

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember3, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
    }


    // 휴면복구 유저가 join 시도 (IBD 의 시도)
    @Test
    void joinMoim_shouldReturn200_whenIBDMemberJoinSuccessful() throws Exception {

        // given - 추가 데이터 - 추가 데이터 제어시에는 항상 영속화 관계 주의하기
        // 주의 : moim 은 전역 함수로 되었고, 준영속 상태로 넘어가 있음
        // 이 때, memberJoinMoim 에 넣으면 그냥 testMoim1 이라는 필드 안에 있는 상태에서 노는 것 --> 영속화가 되어야 아래 em 제어시 날라감
        testMoim1 = em.find(Moim.class, testMoim1.getId());
        MoimMember.memberJoinMoim(testMember, testMoim1, NORMAL, IBD); // 휴면계정 전환으로 인해 나갔었다
        em.flush();
        em.clear();

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
        em.flush();
        em.clear();

        // then - 결과값 확인 필요  - 사실 이 부분이 Service - Repository Test 쪽으로 가도 되는 부분이긴 하다
        Optional<MoimMember> testMoimMember = moimMemberRepository.findByMemberAndMoimId(testMember.getId(), testMoim1.getId());
        assertTrue(testMoimMember.isPresent());
        assertThat(testMoimMember.get().getMemberState()).isEqualTo(ACTIVE); // 가입이 된다
        assertThat(testMoimMember.get().getMoim().getCurMemberCount()).isEqualTo(3);

    }


    // 스스로 나감이 join 시도 (IBW 의 시도)
    @Test
    void joinMoim_shouldReturn200_whenIBWMemberJoinSuccessful() throws Exception {

        // given - 추가 데이터
        testMoim1 = em.find(Moim.class, testMoim1.getId());
        MoimMember.memberJoinMoim(testMember, testMoim1, NORMAL, IBW); // 스스로 나갔었다
        em.flush();
        em.clear();

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
        em.flush();
        em.clear();

        // then - 결과값 확인 필요  - 사실 이 부분이 Service - Repository Test 쪽으로 가도 되는 부분이긴 하다
        Optional<MoimMember> testMoimMember = moimMemberRepository.findByMemberAndMoimId(testMember.getId(), testMoim1.getId());
        assertTrue(testMoimMember.isPresent());
        assertThat(testMoimMember.get().getMemberState()).isEqualTo(ACTIVE); // 가입이 된다
        assertThat(testMoimMember.get().getMoim().getCurMemberCount()).isEqualTo(3);

    }


    // 실패 CASE 작성
    // BODY 가 비어있음 // TODO :: ResponseBody 도 정규 응답이 없음 - 그냥 400 만 Controller 매핑 단에서 스프링이 처리해주는 상황 (위 404와 동일)
    @Test
    void joinMoim_shouldReturn400_whenRequestBodyEmpty() throws Exception {

        // given
        String testAccessToken = createTestJwtToken(testMember3, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());

    }


    // BODY 안의 json 내부 필수 필드가 없음
    @Test
    void joinMoim_shouldReturn400_whenDtoValidationFails_byMoimingValidationException() throws Exception {

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember3, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // 강퇴가 join 시도
    @Test
    void joinMoim_shouldReturn403_whenIBFMemberJoin_byMoimingApiException() throws Exception {

        // given - 추가 데이터
        testMoim1 = em.find(Moim.class, testMoim1.getId()); // 재영속화
        MoimMember.memberJoinMoim(testMember, testMoim1, NORMAL, IBF); // 강퇴당한 유저이다
        em.flush();
        em.clear();

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_JOIN_FORBIDDEN.getErrCode()));

        em.flush();
        em.clear();

        // then - 결과값 확인 필요  - 사실 이 부분이 Service - Repository Test 쪽으로 가도 되는 부분이긴 하다
        Optional<MoimMember> testMoimMember = moimMemberRepository.findByMemberAndMoimId(testMember.getId(), testMoim1.getId());
        assertTrue(testMoimMember.isPresent());
        assertThat(testMoimMember.get().getMemberState()).isEqualTo(IBF); // 여전하다
        assertThat(testMoimMember.get().getMoim().getCurMemberCount()).isEqualTo(2); // 여전히 두명이다

    }


    // 정원 가득찬 모임 (DB상 최소 정원은 3이다)
    // 모든 테스트에서 testMoim 이 필요하진 않으니, 상황에 맞춰서 생성하고 가입시켜 준다
    @Test
    void joinMoim_shouldReturn403_whenMemberJoinFullMoim_byMoimingApiException() throws Exception {

        // given - data su
        Moim testMoim = makeTestMoim(moimName2, 3, moimArea.getState(), moimArea.getCity(), testMoimCategories, testMember1);
        MoimMember.memberJoinMoim(testMember2, testMoim, NORMAL, ACTIVE);
        MoimMember.memberJoinMoim(testMember3, testMoim, NORMAL, ACTIVE);
        em.persist(testMoim);
        em.flush();
        em.clear();

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_JOIN_FAIL_BY_MEMBER_FULL.getErrCode()));

    }


    // 이미 가입되어 있는 사람이 가입하려 함 -> 같은 상태로의 전환 요청은 예외처리된다
    @Test
    void joinMoim_shouldReturn409_whenAlreadyMoimMember_byMoimingApiException() throws Exception {

        // given
        MoimMemberJoinReqDto requestDto = new MoimMemberJoinReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_JOIN)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isConflict());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_JOIN_FAIL_BY_ALREADY_JOINED.getErrCode()));

    }


    // leaveMoim() - 모임 나가기
    // 성공 - 성공 후 DB 도 확인하면 좋을 듯
    @Test
    void leaveMoim_shouldReturn200_whenSuccessful() throws Exception { // testMember2 가 나가려고 한다

        // given
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        em.flush();
        em.clear();

        // then - DB Test 같이 진행 (Service - Repo Test 면 좋은)
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(testMember2.getId(), testMoim1.getId());
        assertTrue(moimMemberOp.isPresent());
        assertThat(moimMemberOp.get().getMemberState()).isEqualTo(IBW);
        assertThat(moimMemberOp.get().getMoim().getCurMemberCount()).isEqualTo(1);

    }


    // 실패 CASE 작성
    // 해당 모임에 있는 모임원을 찾지 못함 - Moim 혹은 Member 어느쪽이 잘못되었는지는 모름
    @Test
    void leaveMoim_shouldReturn404_whenMoimMemberNotFound_byMoimingApiException() throws Exception {

        // given
        Long wrongMoimId = 1000L;
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto(wrongMoimId);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

    }


    // MANAGER 가 나가려 한다 (운영자(생성자)는 나갈 수 없음)
    @Test
    void leaveMoim_shouldReturn403_whenManagerAttemptsLeave_byMoimingApiException() throws Exception {

        // given
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto(testMoim1.getId());
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_LEAVE_FAIL_BY_MANAGER.getErrCode()));

    }


    // BODY 가 비어있다 // TODO :: ResponseBody 도 정규 응답이 없음 - 그냥 400만 Controller 매핑 단에서 스프링이 처리해주는 상황 (위 404와 동일)
    @Test
    void leaveMoim_shouldReturn400_whenBodyEmpty() throws Exception {

        // given
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isBadRequest());

    }


    // BODY 필드가 Validate 실패한다
    @Test
    void leaveMoim_shouldReturn400_whenValidationFails_byMoimingValidationException() throws Exception {

        // given
        MoimMemberLeaveReqDto requestDto = new MoimMemberLeaveReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_LEAVE)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // expelMember() - 모임원 강퇴하기
    // 성공 - 성공 후 DB 도 확인하면 좋을 듯
    @Test
    void expelMember_shouldReturn200_whenSuccessful() throws Exception { // testMember1 이 Member2 를 강퇴한다

        // given
        String inactiveReason = "너가 모임을 못하는 이유는";
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto(testMoim1.getId(), testMember2.getId(), inactiveReason);
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        em.flush();
        em.clear();

        // then - DB Test 같이 진행 (Service - Repo Test 면 좋은)
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(testMember2.getId(), testMoim1.getId());
        assertTrue(moimMemberOp.isPresent());
        assertThat(moimMemberOp.get().getMemberState()).isEqualTo(IBF);
        assertThat(moimMemberOp.get().getInactiveReason()).isEqualTo(inactiveReason);
        assertThat(moimMemberOp.get().getMoim().getCurMemberCount()).isEqualTo(1);

    }


    // 실패 CASE 작성
    // 스스로는 강퇴할 수 없음
    @Test
    void expelMember_shouldReturn422_whenAttemptSelfExpel_byMoimingApiException() throws Exception {

        // given
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto(testMoim1.getId(), testMember1.getId(), "");
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_SITUATION.getErrCode()));
    }


    // 비운영자가 강퇴를 시도함
    @Test
    void expelMember_shouldReturn403_whenAttemptByNoManager_byMoimingApiException() throws Exception {

        // given - 추가 데이터 - 일반 유저가 일반 유저를 강퇴하려는 시도를 TEST 하는게 맞아 보임 - member3 가입시키자
        testMoim1 = em.find(Moim.class, testMoim1.getId());
        MoimMember.memberJoinMoim(testMember3, testMoim1, NORMAL, ACTIVE);
        em.flush();
        em.clear();

        // given
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto(testMoim1.getId(), testMember2.getId(), "");
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember3, 2000); // 일반 유저가 시도한다

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));

    }


    // 모임에 없는 멤버를 강퇴하려고 한다
    @Test
    void expelMember_shouldReturn422_whenAttemptToNoMoimMember_byMoimingApiException() throws Exception {

        // given
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto(testMoim1.getId(), testMember3.getId(), ""); // 가입되어 있지 않은 유저
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000); // 일반 유저가 시도한다

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_SITUATION.getErrCode()));

    }


    // 누군가를 강퇴하는 요청을 보낸 유저가 가입한적 없는 모임이다 // 이걸 먼저 판별하기 때문에 운영자에서 안걸러진다
    @Test
    void expelMember_shouldReturn422_whenAttemptByNoMoimMember_byMoimingApiException() throws Exception {

        // given
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto(testMoim1.getId(), testMember2.getId(), "");
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember3, 2000); // 가입되지 않은 유저의 시도

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

    }


    // BODY 가 비어있다 // TODO :: ResponseBody 도 정규 응답이 없음 - 그냥 400 만 Controller 매핑 단에서 스프링이 처리해주는 상황 (위 404와 동일)
    @Test
    void expelMember_shouldReturn400_whenBodyEmpty() throws Exception {

        // given
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isBadRequest());

    }


    // BODY 필드가 Validate 실패한다
    @Test
    void expelMoim_shouldReturn400_whenValidationFails_byMoimingValidationException() throws Exception {

        // given
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));
        System.out.println("responseBody = " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // BODY 필드가 Validate 실패한다2 - inactiveReason 은 공백 입력값이여도 된다 (NULL금지)
    @Test
    void expelMoim_shouldReturn400_whenValidationFailsWithoutInactiveReason_byMoimingValidationException() throws Exception {

        // given
        MoimMemberExpelReqDto requestDto = new MoimMemberExpelReqDto();
        requestDto.setInactiveReason("");
        String requestBody = om.writeValueAsString(requestDto);
        String testAccessToken = createTestJwtToken(testMember1, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_MEMBER_EXPEL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.inactiveReason").doesNotExist()); // 공백으로 요청되는건 상관 없다

    }

}
