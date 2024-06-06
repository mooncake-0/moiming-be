package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
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

import java.time.LocalDate;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.MoimPostCategory.*;
import static com.peoplein.moiming.domain.file.FileDomain.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// 로직 이슈는 다 뒤에서 점검되었다, Controller Test 만 진행되면 됨
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimPostControllerTest extends TestObjectCreator {

    private final String normalPostTitle = "제목입니다";
    private final String normalPostcontent = "내용은내용과내용입니다";
    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member moimCreator;
    private Member moimMember;
    private Member notMoimMember;
    private Moim testMoim;

    @BeforeEach
    void be() {

        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberPhone, nickname, ci, testRole);
        moimMember = makeTestMember(memberPhone2, nickname2, ci2, testRole);
        notMoimMember = makeTestMember(memberPhone3, nickname3, ci3, testRole);
        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(notMoimMember);

        // Moim Cateogry 저장
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        // Moim 준비
        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMoim);

        em.flush();
        em.clear();

    }

    void insertTestMemberInfo(Member member, MemberGender memberGender, LocalDate memberBirth) {
        // given - 개인정보 기입완료
        MemberInfo curMemberInfo = makeMemberInfo(member, memberGender, memberBirth);
        em.persist(curMemberInfo);
        em.flush();
        em.clear();
    }

    // 1번 createPost() - 생성 성공 확인
    @Test
    void createPost_shouldReturn200_whenRightInfoPassed() throws Exception {

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, REVIEW.getValue(), false, true, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String response = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.moimPostId").exists());
        resultActions.andExpect(jsonPath("$.data.postTitle").value(normalPostTitle));
        resultActions.andExpect(jsonPath("$.data.postContent").value(normalPostcontent));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(REVIEW.getValue()));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(true));
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(false));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(0));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());

        // then notification 생성 확인 - moimMember 가 만들었으니 다른 모임원인 creator 에게 알림이 감
        Notification notification = em.createQuery("SELECT n FROM Notification n WHERE n.receiverId = :receiverId", Notification.class)
                .setParameter("receiverId", testMoim.getCurManagerId())
                .getSingleResult();
        assertNotNull(notification);

        // then notification 생성 확인 - moimMember 가 만들었으니 moimMember 에겐 안감
        List<Notification> notification2 = em.createQuery("SELECT n FROM Notification n WHERE n.receiverId = :receiverId", Notification.class)
                .setParameter("receiverId", moimMember.getId())
                .getResultList();
        assertTrue(notification2.isEmpty());

    }


    // 실패 - createPost : 개인 정보 등록 안된 회원의 요청
    @Test
    void createPost_shouldReturn403_whenNoMemberInfoReq_byMoimingApiException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, REVIEW.getValue(), false, true, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_INFO_NOT_REGISTERED.getErrCode()));

    }


    // moimId Null
    @Test
    void createPost_shouldReturn400_whenMoimIdNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                null, normalPostTitle, normalPostcontent, REVIEW.getValue(), false, true, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
    }


    // PostTitle 작음
    @Test
    void createPost_shouldReturn400_whenMoimTitleNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), null, normalPostcontent, REVIEW.getValue(), false, true, null
        );

        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.postTitle").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // Post Content 작음 // PostTitle 과 같은 Validation 이므로, 한 상황씩 체크한다
    @Test
    void createPost_shouldReturn400_whenPostContentLessThan10_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, "내용은최소10자", REVIEW.getValue(), false, true, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.postContent").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // MPC Null
    @Test
    void createPost_shouldReturn400_whenCategoryValueNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, null, false, true, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));
    }


    // anyBoolean Null
    @Test
    void createPost_shouldReturn400_whenAnyBooleanValueNull_byMoimingValidationException() throws Exception {

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, REVIEW.getValue(), false, null, null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String body = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("body = " + body);


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").exists());
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

    }


    // 240411 - TODO :: imgFileIds 변수 추가 - 세부적인 Test 필요 - 현재 Test 들은 고려되어 있지 않기 때문에 해당 필드 다 Null 처리
    // 오류 - hasFiles - true, imgFileIds null
    @Test
    void createPost_shouldReturn400_whenFileExistsAndIdValidationFails_byMoimingApiException() throws Exception {

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, REVIEW.getValue(), true, true
                , null
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String response = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(IMG_STATE_NOT_VALID.getErrCode()));

    }


    // 오류 - hasFiles - false, imgFileIds 존재
    @Test
    void createPost_shouldReturn400_whenFileNotExistsAndIdValidationFails_byMoimingApiException() throws Exception {

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, REVIEW.getValue(), false, true
                , List.of(1L, 2L) // 안생성해도 Validation 발생 함
        );
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String response = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(IMG_STATE_NOT_VALID.getErrCode()));

    }


    // File 객체를 생성해놓은 채로 테스트 진행
    @Test
    void createPost_shouldReturn200_whenFilesPassed() throws Exception {

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given - save Files
        File file1 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        File file2 = new File(POST, "fileName", "fileUrl", "contentType", 1L);
        em.persist(file1);
        em.persist(file2);
        em.flush();
        em.clear();

        // given
        String testToken = createTestJwtToken(moimMember, 2000);
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(
                testMoim.getId(), normalPostTitle, normalPostcontent, REVIEW.getValue(), true, true
                , List.of(file2.getId(), file1.getId())
        );

        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MOIM_POST_CREATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken)
        );
        String response = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.moimPostId").exists());
        resultActions.andExpect(jsonPath("$.data.postTitle").value(normalPostTitle));
        resultActions.andExpect(jsonPath("$.data.postContent").value(normalPostcontent));
        resultActions.andExpect(jsonPath("$.data.moimPostCategory").value(REVIEW.getValue()));
        resultActions.andExpect(jsonPath("$.data.hasPrivateVisibility").value(true));
        resultActions.andExpect(jsonPath("$.data.hasFiles").value(true));
        resultActions.andExpect(jsonPath("$.data.commentCnt").value(0));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());


        // then - db verify files
        List<RelatedFile> relatedFiles = em.createQuery("SELECT rf FROM RelatedFile rf " +
                                "WHERE rf.fileDomain = :fileDomain " + // 현재 저장된건 이 테스트 결과일 뿐
                                "ORDER BY rf.sequence" // 보낸 순서대로 저장되었어야 한다
                        , RelatedFile.class)
                .setParameter("fileDomain", POST)
                .getResultList();

        assertThat(relatedFiles.size()).isEqualTo(2);
        assertThat(relatedFiles.get(0).getFile().getId()).isEqualTo(file2.getId()); // 2 를 먼저보냈으므로 2가 앞선 sequence 로 저장된다
        assertThat(relatedFiles.get(1).getFile().getId()).isEqualTo(file1.getId());
    }


    // moimId Null 이면 Exception (뒤에서 발생함)
    @Test
    void getMoimPosts_shouldReturn400_whenMoimIdNullURL_byMoimingApiException() throws Exception {

        // given - 다른 상황 때문이 아님을 검증하기 위해 동일하게 SU
        String testToken = createTestJwtToken(moimMember, 3000);
        MoimPostCategory category = GREETING;
        MoimPost samplePost = MoimPost.createMoimPost(normalPostTitle, normalPostcontent, category, false, false, testMoim, moimCreator);
        int limit = 10;
        em.persist(samplePost);
        em.flush();
        em.clear();

        String[] params = {"moimId"};
        String[] vals = {" "};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MOIM_POST_GET_VIEW, params, vals)) // " " 으로 moimId 제공
                .param("lastPostId", samplePost.getId() + "") // 해당값 이후로 출력한다
                .param("category", category.getValue())
                .param("limit", limit + "")
                .header(JwtParams.HEADER, JwtParams.PREFIX + testToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        //then
        resultActions.andExpect(status().isInternalServerError());
    }

}
