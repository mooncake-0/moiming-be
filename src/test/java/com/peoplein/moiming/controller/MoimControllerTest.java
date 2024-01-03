package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.RoleRepository;
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

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private Moim createdMoim;
    private Role testRole;
    private Category testCategory1;
    private Category testCategory1_1;
    private Category testCategory2;
    private Category testCategory2_1;

    @BeforeEach
    void be() {

        // 1번 Member 형성
        testRole = makeTestRole(RoleType.USER);
        curMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci,testRole);

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


        // 2번 1번 Member 가 Moim 형성
        createdMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), curMember);
        moimRepository.save(createdMoim);

        // 3번 Member의 Access Token 발급
        testAccessToken = createTestJwtToken(curMember, 2000);

        em.flush();
        em.clear();

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
    void getMemberMoims_shouldReturn200_whenRightInfoPassed() throws Exception {

        // given
        suAnotherMoim();

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_GET_VIEW).header(HEADER, PREFIX + testAccessToken));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray()); // Collection 이 반환된다
        resultActions.andExpect(jsonPath("$.data", hasSize(2))); // 갯수가 현재 2개의 모임에 가입되어 있다.
        resultActions.andExpect(jsonPath("$.data[*].moimName", hasItem(moimName)))
                .andExpect(jsonPath("$.data[*].moimName", hasItem(moimName2))) // 새로 가입시켜준 모임이 소속 모임 중에 있다
                .andExpect(jsonPath("$.data[*].categories", hasSize(2))); // 카테고리 필드는 2개 있다

    }


    @Test
    void getMemberMoims_shouldReturn200_whenRightInfoPassedAndNoMoimJoined() throws Exception {

        // given
        Member curMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2,testRole);
        memberRepository.save(curMember2);
        em.flush();
        em.clear();

        String testAccessToken2 = createTestJwtToken(curMember2, 1000); // 아무 모임도 가입하지 않은 회원이 요청한다

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MOIM_GET_VIEW).header(HEADER, PREFIX + testAccessToken2));
        System.out.println("responseBody: " + resultActions.andReturn().getResponse().getContentAsString());


        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isArray()); // Collection 이 반환된다
        resultActions.andExpect(jsonPath("$.data", hasSize(0))); // 갯수가 현재 2개의 모임에 가입되어 있다.
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
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));

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
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));

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
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
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
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));

    }

}