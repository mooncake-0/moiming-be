package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimControllerTest extends TestObjectCreator {

    public final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    // 실제 간이 저장 필요, Mock 이 아닌 Autowire 필요
    @Autowired
    private EntityManager em;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MoimRepository moimRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    private String testAccessToken;
    private Member curMember;
    private Member testMember2;
    private Moim createdMoim;
    private Role testRole;
    private Category testCategory1;
    private Category testCategory1_1;
    private Category testCategory2;
    private Category testCategory2_1;

    private List<Category> categories = new ArrayList<>();

    @BeforeEach
    void be() {

        // 1번 Member 형성
        testRole = makeTestRole(RoleType.USER);
        curMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        roleRepository.save(testRole);
        memberRepository.save(curMember);


        // 모임 카테고리 사전생성
        testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 2, testCategory1);
        testCategory2 = new Category(3L, CategoryName.fromValue(depth1SampleCategory2), 1, null);
        testCategory2_1 = new Category(4L, CategoryName.fromValue(depth2SampleCategory2), 2, testCategory2);
        categoryRepository.save(testCategory1);
        categoryRepository.save(testCategory1_1);
        categoryRepository.save(testCategory2);
        categoryRepository.save(testCategory2_1);

        categories.add(testCategory1);
        categories.add(testCategory1_1);

        // 2번 1번 Member 가 Moim 형성
        createdMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), categories, curMember);
        moimRepository.save(createdMoim);

        // 3번 Member의 Access Token 발급
        testAccessToken = createTestJwtToken(curMember, 2000);

        em.flush();
        em.clear();

    }


    void makeAnotherMember() {
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        em.persist(testMember2);

        em.flush();
        em.clear();
    }


    /*
     정렬을 잘 확인하기 위해서,
     일부 Thread Sleep 을 달아준다
     */
    // 내가 주인인 Moim 을 만든다
    void buildManagingMoims(int END) throws InterruptedException {
        Moim lastMoim = null;
        for (int i = 0; i < END; i++) {
            lastMoim = makeTestMoim(moimName + i, maxMember, moimArea.getState(), moimArea.getCity(), categories, curMember);
            em.persist(lastMoim);
            if (i % 3 == 0) {
                Thread.sleep(100);
            }
        }
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        lastMoim.setMoimJoinRule(joinRule);
    }


    // 나는 다른 Moim 에 Join 한다
    void buildOtherMoims(int END, int END2) throws InterruptedException {

        for (int i = 0; i < END2; i++) { // ACTIVE 하지 않은 모임들도 추가해보자
            Moim moim = makeTestMoim(moimName + ", 탈퇴한 모임임 " + i, maxMember, moimArea.getState(), moimArea.getCity(), categories, testMember2);
            MoimMember.memberJoinMoim(curMember, moim, MoimMemberRoleType.NORMAL, MoimMemberState.IBW);
            em.persist(moim);
        }

        for (int i = 0; i < END; i++) {
            Moim moim = makeTestMoim(moimName + ", 일반회원임 " + i, maxMember, moimArea.getState(), moimArea.getCity(), categories, testMember2);
            MoimMember.memberJoinMoim(curMember, moim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
            em.persist(moim);
            Thread.sleep(100);
        }
    }


    @Test
    void createMoim_shouldReturn201_whenMoimReqNoJoinRulePassed() throws Exception {

        // given
        MoimCreateReqDto reqDto = makeMoimReqDtoNoJoinRule(moimName2, moimArea2.getState(), moimArea2.getCity(), maxMember2, depth1SampleCategory2, depth2SampleCategory2);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(responseBody);

        // then
        // responseBody : Data MoimCreateRespDto 전달
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.joinRule").isEmpty());
        resultActions.andExpect(jsonPath("$.data.curMemberCount").value(1));


    }


    // JoinRule 있을 때도 성공하는지
    @Test
    void createMoim_shouldReturn201_whenMoimReqWithJoinRulePassed() throws Exception {

        // given
        MoimCreateReqDto reqDto = makeMoimReqDtoWithJoinRule(moimName2, moimArea2.getState(), moimArea2.getCity(), maxMember2, depth1SampleCategory2, depth2SampleCategory2);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.joinRule.hasAgeRule").value(true));
        resultActions.andExpect(jsonPath("$.data.joinRule.ageMax").value(40));
        resultActions.andExpect(jsonPath("$.data.curMemberCount").value(1));
    }


    // validation 값 오류
    // moimName
    @Test
    void createMoim_shouldReturn400_whenNameFieldWrong_byMoimingValidationException() throws Exception {

        // given
        MoimCreateReqDto reqDto = makeMoimReqDtoNoJoinRule("WRG", moimArea2.getState(), moimArea2.getCity(), maxMember2, depth1SampleCategory2, depth2SampleCategory2);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());

    }


    // category 안감
    @Test
    void createMoim_shouldReturn400_whenCategoryMissing_byMoimingValidationException() throws Exception {

        // given
        MoimCreateReqDto reqDto = makeMoimReqDtoNoJoinRule(moimName2, moimArea2.getState(), moimArea2.getCity(), maxMember2, depth1SampleCategory2, depth2SampleCategory2);
        reqDto.setCategoryNameValues(null); // 해당 필드 없애기
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
    }


    // category 1개만 감
    @Test
    void createMoim_shouldReturn400_whenOnlyOneCategory_byMoimingValidationException() throws Exception {

        // given
        MoimCreateReqDto reqDto = makeMoimReqDtoNoJoinRule(moimName2, moimArea2.getState(), moimArea2.getCity(), maxMember2, depth1SampleCategory2, depth2SampleCategory2);
        reqDto.setCategoryNameValues(List.of(depth1SampleCategory2)); // 한 개 있는 리스트 필드로 변경한다
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
    }


    // join rule Age Rule 정보 없음
    @Test
    void createMoim_shouldReturn400_whenJoinRuleFieldMissing_byMoimingValidationException() throws Exception {

        // given
        MoimCreateReqDto reqDto = makeMoimReqDtoWithJoinRule("WRG", moimArea2.getState(), moimArea2.getCity(), maxMember2, depth1SampleCategory2, depth2SampleCategory2);
        reqDto.getJoinRuleDto().setHasAgeRule(null);

        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());

    }


    // category 연결 오류 // 이건 CategoryService 에서 나는 오류로, Test 안해도 됨.
    // -------------------------------- 유저의 모임 일반조회

    private void suAnotherMoim() { // 필요시 모임을 하나 더 가입시키기 위한 GIVEN 상황

        Moim anotherMoim = makeTestMoim(moimName2, maxMember2, moimArea2.getState(), moimArea2.getCity(), List.of(testCategory2, testCategory2_1), curMember);
        moimRepository.save(anotherMoim);
        em.flush();
        em.clear();

    }


    @Test
    void getMemberMoims_shouldReturn200WithSortedMoims_whenFirstReq() throws Exception {

        //given
        makeAnotherMember();
        buildManagingMoims(25);
        buildOtherMoims(5, 5);
        em.flush();
        em.clear();

        // when - limit 과 isManagerReq 는 기본으로 들어감, lastMoimId 는 첫 요청이라 없음
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_GET_VIEW)
                .header(HEADER, PREFIX + testAccessToken)
        );

        // then
        // JSON 안에 있는 Value 들이 Sorting 되어 있는지?
        String resp = resultActions.andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = om.readTree(resp);
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("data");

        for (int i = 0; i < arrayNode.size() - 1; i++) {
            JsonNode node = arrayNode.get(i);
            JsonNode nextNode = arrayNode.get(i + 1);
            LocalDateTime nodeTime = LocalDateTime.parse(node.get("createdAt").asText());
            LocalDateTime nextNodeTime = LocalDateTime.parse(nextNode.get("createdAt").asText());
            assertTrue(nodeTime.isAfter(nextNodeTime) || nodeTime.isEqual(nextNodeTime)); // 같거나 빠른 날짜 순서대로 정렬된다
        }

        assertThat(arrayNode.size()).isEqualTo(20);
    }


    // 두번의 요청을 연속해서 수행한다 - 정확한 테스트를 위함
    @Test
    void getMemberMoims_shouldReturn200WithNextMoims_whenNextPagingReqSent() throws Exception {

        //given
        makeAnotherMember();
        buildManagingMoims(25);
        buildOtherMoims(5, 5);
        em.flush();
        em.clear();

        // when - limit 과 isManagerReq 는 기본으로 들어감, lastMoimId 는 첫 요청이라 없음
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_GET_VIEW)
                .header(HEADER, PREFIX + testAccessToken)
        );

        String resp = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = om.readTree(resp);
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("data");
        long lastMoimId = arrayNode.get(arrayNode.size() - 1).get("moimId").asLong();


        // 위는 위 테스트에서 확인함. when -2 를 날린다
        // when - 2 이번엔 그냥 다 넣어서 날려보자
        ResultActions resultActions2 = mvc.perform(get(PATH_MOIM_GET_VIEW)
                .header(HEADER, PREFIX + testAccessToken)
                .param("lastMoimId", String.valueOf(lastMoimId))
                .param("isManagerReq", String.valueOf(false))
                .param("limit", String.valueOf(20))
        );

        String resp2 = resultActions2.andReturn().getResponse().getContentAsString();

        JsonNode jsonNode2 = om.readTree(resp2);
        ArrayNode arrayNode2 = (ArrayNode) jsonNode2.get("data");

        for (int i = 0; i < arrayNode2.size() - 1; i++) {
            JsonNode node = arrayNode2.get(i);
            JsonNode nextNode = arrayNode2.get(i + 1);
            LocalDateTime nodeTime = LocalDateTime.parse(node.get("createdAt").asText());
            LocalDateTime nextNodeTime = LocalDateTime.parse(nextNode.get("createdAt").asText());
            assertTrue(nodeTime.isAfter(nextNodeTime) || nodeTime.isEqual(nextNodeTime)); // 같거나 빠른 날짜 순서대로 정렬된다
        }

        assertThat(arrayNode.size()).isEqualTo(20);
        assertThat(arrayNode2.size()).isEqualTo(11);

    }


    @Test
    void getMemberMoims_shouldReturn200WithOnlyMyManagingMoims_whenRightInfoPassed() throws Exception {

        //given
        makeAnotherMember();
        buildManagingMoims(10); // 10 개만 받아지는지 확인한다
        buildOtherMoims(10, 5);
        em.flush();
        em.clear();

        // when - limit 과 isManagerReq 는 기본으로 들어감, lastMoimId 는 첫 요청이라 없음
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_GET_VIEW)
                .header(HEADER, PREFIX + testAccessToken)
                .param("isManagerReq", String.valueOf(true))
                .param("limit", String.valueOf(20))
        );

        // then
        // JSON 안에 있는 Value 들이 Sorting 되어 있는지?
        String resp = resultActions.andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = om.readTree(resp);
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("data");

        for (int i = 0; i < arrayNode.size() - 1; i++) {
            JsonNode node = arrayNode.get(i);
            JsonNode nextNode = arrayNode.get(i + 1);
            LocalDateTime nodeTime = LocalDateTime.parse(node.get("createdAt").asText());
            LocalDateTime nextNodeTime = LocalDateTime.parse(nextNode.get("createdAt").asText());
            assertTrue(nodeTime.isAfter(nextNodeTime) || nodeTime.isEqual(nextNodeTime)); // 같거나 빠른 날짜 순서대로 정렬된다
        }

        assertThat(arrayNode.size()).isEqualTo(11);
    }


    ////// update 요청 TEST
    @Test
    void updateMoim_shouldReturn200_whenRightInfoPassed() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(createdMoim.getId(), moimName2, maxMember2, moimArea2.getState(), depth1SampleCategory2, depth2SampleCategory2);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.moimName").value(moimName2)); // 변경됨
        resultActions.andExpect(jsonPath("$.data.moimInfo").value(moimInfo)); // 변경되지 않음
        resultActions.andExpect(jsonPath("$.data.categories[*]", hasItem(depth1SampleCategory2))); // 변경된 Category 를 포함하고 있다

        // updator 확인
        assertThat(createdMoim.getUpdaterId()).isEqualTo(curMember.getId());

    }

    // moimId Validation
    @Test
    void updateMoim_shouldReturn400_whenMoimIdMissing_byMoimingValidationException() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(null, moimName2, maxMember2, moimArea2.getState(), depth1SampleCategory2, depth2SampleCategory2);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // moimName Validation
    @Test
    void updateMoim_shouldReturn400_whenMoimNameWrong_byMoimingValidationException() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(createdMoim.getId(), "WRNG", null, null, depth1SampleCategory2, depth2SampleCategory2);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // maxMember Validation
    @Test
    void updateMoim_shouldReturn400_whenMaxMemberWrong_byMoimingValidationException() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(createdMoim.getId(), null, 101, null, "", ""); // 사실 인 앱으로 안가기 때문에 아무거나 넣어도 될듯
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
    }


    // 수정하려는 필드가 없을 경우
    @Test
    void updateMoim_shouldReturn200_whenNoValuesEdited() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(createdMoim.getId(), null, null, null, depth1SampleCategory2, depth1SampleCategory2);
        reqDto.setCategoryNameValues(null); // Category List 는 이렇게 필드 없음을 명시해야함 - null 통과시 List.of 함수 때문에 NULL 통과 불가
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.moimName").value(moimName)); // 변경됨
        resultActions.andExpect(jsonPath("$.data.moimInfo").value(moimInfo)); // 변경되지 않음
        resultActions.andExpect(jsonPath("$.data.categories[*]", hasItem(depth1SampleCategory))); // 변경되기 전  Category 를 포함하고 있다

        // updator 확인 // 수정된게 없어도 수정자 설정은 들어가도록 설계함 > 필요시 변경하기
        assertThat(createdMoim.getUpdaterId()).isEqualTo(curMember.getId());
    }


    // Category Null Validation > 통과해야함 (수정하지 않겠다는 뜻)
    @Test
    void updateMoim_shouldReturn200_whenCategoryFieldNull() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(createdMoim.getId(), moimName2, maxMember2, moimArea2.getState(), depth1SampleCategory2, depth2SampleCategory2);
        reqDto.setCategoryNameValues(null);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.moimName").value(moimName2)); // 변경됨
        resultActions.andExpect(jsonPath("$.data.moimInfo").value(moimInfo)); // 변경되지 않음
        resultActions.andExpect(jsonPath("$.data.categories[*]", hasItem(depth1SampleCategory))); // 변경되기 전 Category 를 포함하고 있다


        // updator 확인 // Category 외 수정한게 있음
        assertThat(createdMoim.getUpdaterId()).isEqualTo(curMember.getId());
    }


    // Category Num Validation > 잘못됨
    @Test
    void updateMoim_shouldReturn400_whenCategoryWrong_byMoimingValidationException() throws Exception {

        // given
        MoimUpdateReqDto reqDto = makeMoimUpdateReqDto(createdMoim.getId(), moimName2, maxMember2, moimArea2.getState(), depth1SampleCategory2, depth2SampleCategory2);
        reqDto.setCategoryNameValues(List.of(depth1SampleCategory2));
        String requestBody = om.writeValueAsString(reqDto);


        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MOIM_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // getMoimDetail - 성공, 세부 조회
    @Test
    void getMoimDetail_shouldReturn200WithResponse_whenMoimWithRuleAndRightInfoPassed() throws Exception {

        // given
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);
        em.flush();
        em.clear();

        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_GET_DETAIL, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.moimId").value(createdMoim.getId()));
        resultActions.andExpect(jsonPath("$.data.moimName").value(createdMoim.getMoimName()));
        resultActions.andExpect(jsonPath("$.data.moimInfo").value(createdMoim.getMoimInfo()));
        resultActions.andExpect(jsonPath("$.data.curMemberCount").value(createdMoim.getCurMemberCount()));
        resultActions.andExpect(jsonPath("$.data.maxMember").value(createdMoim.getMaxMember()));
        resultActions.andExpect(jsonPath("$.data.areaCity").value(createdMoim.getMoimArea().getCity()));
        resultActions.andExpect(jsonPath("$.data.areaState").value(createdMoim.getMoimArea().getState()));
        resultActions.andExpect(jsonPath("$.data.createdAt").value(createdMoim.getCreatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.updatedAt").value(createdMoim.getUpdatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.categories").isArray());
        resultActions.andExpect(jsonPath("$.data.categories", hasItems(testCategory1.getCategoryName().getValue(), testCategory1_1.getCategoryName().getValue())));
        resultActions.andExpect(jsonPath("$.data.joinRule").exists());
        resultActions.andExpect(jsonPath("$.data.joinRule.hasAgeRule").value(true));
        resultActions.andExpect(jsonPath("$.data.joinRule.ageMax").value(40));
        resultActions.andExpect(jsonPath("$.data.joinRule.ageMin").value(20));
        resultActions.andExpect(jsonPath("$.data.joinRule.memberGender").value(MemberGender.N + ""));
        resultActions.andExpect(jsonPath("$.data.creatorInfo.memberId").value(curMember.getId()));
        resultActions.andExpect(jsonPath("$.data.creatorInfo.nickname").value(curMember.getNickname()));

    }


    // getMoimDetail - 가입 조건 없음
    @Test
    void getMoimDetail_shouldReturn200WithResponse_whenMoimNoRuleAndRightInfoPassed() throws Exception {

        // given
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_GET_DETAIL, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.moimId").value(createdMoim.getId()));
        resultActions.andExpect(jsonPath("$.data.moimName").value(createdMoim.getMoimName()));
        resultActions.andExpect(jsonPath("$.data.moimInfo").value(createdMoim.getMoimInfo()));
        resultActions.andExpect(jsonPath("$.data.curMemberCount").value(createdMoim.getCurMemberCount()));
        resultActions.andExpect(jsonPath("$.data.maxMember").value(createdMoim.getMaxMember()));
        resultActions.andExpect(jsonPath("$.data.areaCity").value(createdMoim.getMoimArea().getCity()));
        resultActions.andExpect(jsonPath("$.data.areaState").value(createdMoim.getMoimArea().getState()));
        resultActions.andExpect(jsonPath("$.data.createdAt").value(createdMoim.getCreatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.updatedAt").value(createdMoim.getUpdatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.categories").isArray());
        resultActions.andExpect(jsonPath("$.data.categories", hasItems(testCategory1.getCategoryName().getValue(), testCategory1_1.getCategoryName().getValue())));
        resultActions.andExpect(jsonPath("$.data.joinRule").isEmpty());
        resultActions.andExpect(jsonPath("$.data.creatorInfo.memberId").value(curMember.getId()));
        resultActions.andExpect(jsonPath("$.data.creatorInfo.nickname").value(curMember.getNickname()));

    }


    // getMoimDetail - 해당 모임의 모임원이 아니여도 조회 가능
    @Test
    void getMoimDetail_shouldReturn200WithResponse_whenNoMoimMemberRequest() throws Exception {

        // given
        makeAnotherMember();
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};
        String accessToken2 = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_GET_DETAIL, beforeParam, afterParam))
                .header(HEADER, PREFIX + accessToken2));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.moimId").value(createdMoim.getId()));
        resultActions.andExpect(jsonPath("$.data.moimName").value(createdMoim.getMoimName()));
        resultActions.andExpect(jsonPath("$.data.moimInfo").value(createdMoim.getMoimInfo()));
        resultActions.andExpect(jsonPath("$.data.curMemberCount").value(createdMoim.getCurMemberCount()));
        resultActions.andExpect(jsonPath("$.data.maxMember").value(createdMoim.getMaxMember()));
        resultActions.andExpect(jsonPath("$.data.areaCity").value(createdMoim.getMoimArea().getCity()));
        resultActions.andExpect(jsonPath("$.data.areaState").value(createdMoim.getMoimArea().getState()));
        resultActions.andExpect(jsonPath("$.data.createdAt").value(createdMoim.getCreatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.updatedAt").value(createdMoim.getUpdatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.categories").isArray());
        resultActions.andExpect(jsonPath("$.data.categories", hasItems(testCategory1.getCategoryName().getValue(), testCategory1_1.getCategoryName().getValue())));
        resultActions.andExpect(jsonPath("$.data.joinRule").isEmpty());
        resultActions.andExpect(jsonPath("$.data.creatorInfo.memberId").value(curMember.getId()));
        resultActions.andExpect(jsonPath("$.data.creatorInfo.nickname").value(curMember.getNickname()));

    }


    // getMoimDetail - 실패 : 없는 모임을 세부 조회하려고 함
    @Test
    void getMoimDetail_shouldReturn404_whenRequestNonExistingMoim_byMoimingApiException() throws Exception {

        // given
        String[] beforeParam = {"moimId"};
        String[] afterParam = {12345L + ""};


        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_GET_DETAIL, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_NOT_FOUND.getErrCode()));
    }


    // deleteMoim - 성공 : 모임만 있음
    @Test
    void deleteMoim_shouldReturn200_whenSuccessfulWithOnlyMoim() throws Exception {

        // given
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();

        Moim moim = em.find(Moim.class, createdMoim.getId());
        assertNull(moim);
    }


    // deleteMoim - 성공 : 모임만 있음 + 가입 조건 있음
    @Test
    void deleteMoim_shouldReturn200_whenSuccessfulWithJoinRule() throws Exception {

        // given
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);
        em.flush();
        em.clear();

        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();
        Moim moim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule moimJoinRule = em.find(MoimJoinRule.class, joinRule.getId());
        assertNull(moim);
        assertNull(moimJoinRule);

    }


    // deleteMoim - 성공 : 모임만 있음 + 가입 조건 있음 + 게시물 있음
    @Test
    void deleteMoim_shouldReturn200_whenSuccessfulWithJoinRuleAndPost() throws Exception {

        // given - join Rule
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);

        // given - post
        MoimPost post1 = makeMoimPost(createdMoim, curMember, MoimPostCategory.GREETING, false);
        em.persist(post1);

        em.flush();
        em.clear();


        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();
        Moim moim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule moimJoinRule = em.find(MoimJoinRule.class, joinRule.getId());
        MoimPost dPost1 = em.find(MoimPost.class, post1.getId());
        assertNull(moim);
        assertNull(moimJoinRule);
        assertNull(dPost1);

    }


    // deleteMoim - 성공 : 모임만 있음 + 가입 조건 있음 + 게시물 있음 + 모임원 2명 추가됨
    @Test
    void deleteMoim_shouldReturn200_whenSuccessfulWithJoinRuleAndPostAndMembers() throws Exception {

        // given - join Rule
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);

        // given - post
        MoimPost post1 = makeMoimPost(createdMoim, curMember, MoimPostCategory.GREETING, false);
        em.persist(post1);

        // given - MoimMembers
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        Member testMember3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);

        MoimMember moimMember2 = MoimMember.memberJoinMoim(testMember2, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember moimMember3 = MoimMember.memberJoinMoim(testMember3, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMember2);
        em.persist(testMember3);

        em.flush();
        em.clear();

        // db 중간 checking
//        createdMoim = em.find(Moim.class, createdMoim.getId());
//        List<MoimMember> moimMembers = createdMoim.getMoimMembers();
//        for (MoimMember moimMember : moimMembers) {
//            System.out.println("moimMember.getMember().getNickname() = " + moimMember.getMember().getNickname());
//        }
//        System.out.println("createdMoim = " + createdMoim.getCurMemberCount());


        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();
        assertNull(em.find(Moim.class, createdMoim.getId()));
        assertNull(em.find(MoimJoinRule.class, joinRule.getId()));
        assertNull(em.find(MoimPost.class, post1.getId()));
        assertNull(em.find(MoimMember.class, moimMember2.getId()));
        assertNull(em.find(MoimMember.class, moimMember3.getId()));

    }


    // deleteMoim - 성공 : 모임만 있음 + 가입 조건 있음 + 게시물 2개 있음 (모임원들이 게시물 생성)
    @Test
    void deleteMoim_shouldReturn200_whenSuccessfulWithJoinRuleAndPostsOfMembers() throws Exception {

        // given - join Rule
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);

        // given - post
        MoimPost post1 = makeMoimPost(createdMoim, curMember, MoimPostCategory.GREETING, false);
        em.persist(post1);

        // given - MoimMembers
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        Member testMember3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);

        MoimMember moimMember2 = MoimMember.memberJoinMoim(testMember2, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember moimMember3 = MoimMember.memberJoinMoim(testMember3, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMember2);
        em.persist(testMember3);

        // given - other posts
        MoimPost post2 = makeMoimPost(createdMoim, testMember2, MoimPostCategory.EXTRA, false);
        MoimPost post3 = makeMoimPost(createdMoim, testMember3, MoimPostCategory.REVIEW, true);
        em.persist(post2);
        em.persist(post3);

        em.flush();
        em.clear();

        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();
        assertNull(em.find(Moim.class, createdMoim.getId()));
        assertNull(em.find(MoimJoinRule.class, joinRule.getId()));
        assertNull(em.find(MoimPost.class, post1.getId()));
        assertNull(em.find(MoimPost.class, post2.getId()));
        assertNull(em.find(MoimPost.class, post3.getId()));
        assertNull(em.find(MoimMember.class, moimMember2.getId()));
        assertNull(em.find(MoimMember.class, moimMember3.getId()));

    }


    // deleteMoim - 성공 : 모임만 있음 + 가입 조건 있음 + 게시물 2개 있음 + 각각 댓글들이 있음 (모임원들이 댓글들 생성)
    @Test
    void deleteMoim_shouldReturn200_whenSuccessfulWithJoinRuleAndPostsOfMembersAndComments() throws Exception {

        // given - join Rule
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);

        // given - post
        MoimPost post1 = makeMoimPost(createdMoim, curMember, MoimPostCategory.GREETING, false);
        em.persist(post1);

        // given - MoimMembers
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        Member testMember3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);

        MoimMember moimMember2 = MoimMember.memberJoinMoim(testMember2, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        MoimMember moimMember3 = MoimMember.memberJoinMoim(testMember3, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMember2);
        em.persist(testMember3);

        // given - other posts
        MoimPost post2 = makeMoimPost(createdMoim, testMember2, MoimPostCategory.EXTRA, false);
        MoimPost post3 = makeMoimPost(createdMoim, testMember3, MoimPostCategory.REVIEW, true);
        em.persist(post2);
        em.persist(post3);

        // given - comments 9 개 추가
        // 1 번 게시물에 2 가 댓글, 1이 답글, 3이 댓글, 1이 답글
        // 2 번 게시물에 1 이 댓글, 2가 답글
        // 3 번 게시물에 1 이 댓글, 2가 댓글, 3이 답글
        PostComment comment1 = makePostComment(testMember2, post1, 0, null);
        PostComment comment2 = makePostComment(curMember, post1, 1, comment1);
        PostComment comment3 = makePostComment(testMember3, post1, 0, null);
        PostComment comment4 = makePostComment(curMember, post1, 1, comment3);

        PostComment comment5 = makePostComment(curMember, post2, 0, null);
        PostComment comment6 = makePostComment(testMember2, post2, 1, comment5);

        PostComment comment7 = makePostComment(curMember, post3, 0, null);
        PostComment comment8 = makePostComment(testMember2, post3, 0, null);
        PostComment comment9 = makePostComment(testMember3, post3, 1, comment8);

        em.flush();
        em.clear();

//        MoimPost posta = em.find(MoimPost.class, post1.getId());
//        MoimPost postb = em.find(MoimPost.class, post2.getId());
//        MoimPost postc = em.find(MoimPost.class, post3.getId());
//        List<PostComment> postCommenta = posta.getPostComments();
//        List<PostComment> postCommentb = postb.getPostComments();
//        List<PostComment> postCommentc = postc.getPostComments();
//        for (PostComment comment : postCommenta) {
//            System.out.println("comment.getDepth() = " + comment.getDepth());
//            System.out.println("comment.getContent() = " + comment.getContent());
//        }
//        for (PostComment comment : postCommentb) {
//            System.out.println("comment.getDepth() = " + comment.getDepth());
//            System.out.println("comment.getContent() = " + comment.getContent());
//        }
//        for (PostComment comment : postCommentc) {
//            System.out.println("comment.getDepth() = " + comment.getDepth());
//            System.out.println("comment.getContent() = " + comment.getContent());
//        }
//
//        System.out.println("posta.getCommentCnt() = " + posta.getCommentCnt());
//        System.out.println("postb.getCommentCnt() = " + postb.getCommentCnt());
//        System.out.println("postc.getCommentCnt() = " + postc.getCommentCnt());

        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();
        assertNull(em.find(Moim.class, createdMoim.getId()));
        assertNull(em.find(MoimJoinRule.class, joinRule.getId()));
        assertNull(em.find(MoimPost.class, post1.getId()));
        assertNull(em.find(MoimPost.class, post2.getId()));
        assertNull(em.find(MoimPost.class, post3.getId()));
        assertNull(em.find(MoimMember.class, moimMember2.getId()));
        assertNull(em.find(MoimMember.class, moimMember3.getId()));
        assertNull(em.find(PostComment.class, comment1.getId()));
        assertNull(em.find(PostComment.class, comment2.getId()));
        assertNull(em.find(PostComment.class, comment3.getId()));
        assertNull(em.find(PostComment.class, comment4.getId()));
        assertNull(em.find(PostComment.class, comment5.getId()));
        assertNull(em.find(PostComment.class, comment6.getId()));
        assertNull(em.find(PostComment.class, comment7.getId()));
        assertNull(em.find(PostComment.class, comment8.getId()));
        assertNull(em.find(PostComment.class, comment9.getId()));

    }


    // 실패 : 비모임원 시도
    @Test
    void deleteMoim_shouldReturn404_whenNotMoimMemberReq_byMoimingApiException() throws Exception {

        // given - join Rule
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);

        // given - MoimMembers
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        Member testMember3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);

        MoimMember moimMember2 = MoimMember.memberJoinMoim(testMember2, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMember2);
        em.persist(testMember3);

        em.flush();
        em.clear();

        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};
        String nonMoimMemberToken = createTestJwtToken(testMember3, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + nonMoimMemberToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_FOUND.getErrCode()));

        // then - db verify
        em.flush();
        em.clear();
        assertNotNull(em.find(Moim.class, createdMoim.getId()));

    }


    // 실패 : 일반 모임원 시도
    @Test
    void deleteMoim_shouldReturn403_whenNormalMoimMemberReq_byMoimingApiException() throws Exception {

        // given - join Rule
        createdMoim = em.find(Moim.class, createdMoim.getId());
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        createdMoim.setMoimJoinRule(joinRule);

        // given - MoimMembers
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        MoimMember.memberJoinMoim(testMember2, createdMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMember2);

        em.flush();
        em.clear();

        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {createdMoim.getId() + ""};
        String normalMoimMemberToken = createTestJwtToken(testMember2, 2000);

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + normalMoimMemberToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_MEMBER_NOT_AUTHORIZED.getErrCode()));

        // then - db verify
        em.flush();
        em.clear();
        assertNotNull(em.find(Moim.class, createdMoim.getId()));

    }


    // 실패 : 모임을 찾을 수 없음
    @Test
    void deleteMoim_shouldReturn404_whenMoimNotFound_byMoimingApiException() throws Exception {

        // given start
        String[] beforeParam = {"moimId"};
        String[] afterParam = {12345L + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MOIM_DELETE, beforeParam, afterParam))
                .header(HEADER, PREFIX + testAccessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MOIM_NOT_FOUND.getErrCode()));

        // then - db verify
        em.flush();
        em.clear();
        assertNotNull(em.find(Moim.class, createdMoim.getId()));
    }

}