package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.Report;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.security.token.JwtParams;
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

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.ReportTarget.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;
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
public class ReportControllerTest extends TestObjectCreator {

    public final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member, member2;

    private Moim moim;

    private MoimPost moimPost;

    void dataSu() {

        Role role = makeTestRole(RoleType.USER);
        member = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, role);
        member2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, role);
        em.persist(role);
        em.persist(member);
        em.persist(member2);


        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        moim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), member);
        em.persist(moim);

        moimPost = makeMoimPost(moim, member, MoimPostCategory.NOTICE, false);
        em.persist(moimPost);

        em.flush();
        em.clear();

    }


    // 각각 DB 확인 진행
    // 성공 - member 가 member2 를 신고
    @Test
    void report_shouldReturn201_whenMemberReportsOtherMember() throws Exception {

        // given
        dataSu();
        ReportReason targetReason = ReportReason.ABUSE;
        ReportCreateReqDto requestDto = new ReportCreateReqDto(USER, member2.getId(), targetReason.getIndex(), false);
        String accessToken = createTestJwtToken(member, 2000);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_CREATE_REPORT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isCreated());

        // then - db verify (그냥 저장됐는지만 확인하면 됨)
        em.flush();
        em.clear();

        Report report = em.createQuery("SELECT r FROM Report r WHERE r.reporterId = :memberId", Report.class)
                .setParameter("memberId", member.getId())
                .getSingleResult();
        assertThat(report.getTarget()).isEqualTo(USER);
        assertThat(report.getReporterId()).isEqualTo(member.getId());
        assertThat(report.getTargetId()).isEqualTo(member2.getId());
        assertThat(report.getReason()).isEqualTo(targetReason.getInfo());

    }


    // 성공 - member 가 moim 을 신고
    @Test
    void report_shouldReturn201_whenMemberReportsMoim() throws Exception {

        // given
        dataSu();
        ReportReason targetReason = ReportReason.INAPPROPRIATE_FOR_MINORS;
        ReportCreateReqDto requestDto = new ReportCreateReqDto(MOIM, moim.getId(), targetReason.getIndex(), false);
        String accessToken = createTestJwtToken(member, 2000);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_CREATE_REPORT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isCreated());

        // then - db verify (그냥 저장됐는지만 확인하면 됨)
        em.flush();
        em.clear();

        Report report = em.createQuery("SELECT r FROM Report r WHERE r.reporterId = :memberId", Report.class)
                .setParameter("memberId", member.getId())
                .getSingleResult();
        assertThat(report.getTarget()).isEqualTo(MOIM);
        assertThat(report.getReporterId()).isEqualTo(member.getId());
        assertThat(report.getTargetId()).isEqualTo(moim.getId());
        assertThat(report.getReason()).isEqualTo(targetReason.getInfo());

    }


    // 성공 - member 가 post 를 신고
    @Test
    void report_shouldReturn201_whenMemberReportsPost() throws Exception {

        // given
        dataSu();
        ReportReason targetReason = ReportReason.INAPPROPRIATE_FOR_MINORS;
        ReportCreateReqDto requestDto = new ReportCreateReqDto(POST, moimPost.getId(), targetReason.getIndex(), false);
        String accessToken = createTestJwtToken(member, 2000);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_CREATE_REPORT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isCreated());

        // then - db verify (그냥 저장됐는지만 확인하면 됨)
        em.flush();
        em.clear();

        Report report = em.createQuery("SELECT r FROM Report r WHERE r.reporterId = :memberId", Report.class)
                .setParameter("memberId", member.getId())
                .getSingleResult();
        assertThat(report.getTarget()).isEqualTo(POST);
        assertThat(report.getReporterId()).isEqualTo(member.getId());
        assertThat(report.getTargetId()).isEqualTo(moimPost.getId());
        assertThat(report.getReason()).isEqualTo(targetReason.getInfo());

    }


    // 실패 - 신고 Request Validation
    @Test
    void report_shouldReturn400_whenRequestValidationFail_byMoimingValidationException() throws Exception {

        // given
        dataSu();
        ReportReason targetReason = ReportReason.INAPPROPRIATE_FOR_MINORS;
        ReportCreateReqDto requestDto = new ReportCreateReqDto(null, null, null, null);
        String accessToken = createTestJwtToken(member, 2000);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_CREATE_REPORT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(4)));

    }


    // 실패 - 신고 index 가 잘못됨 - Reason 형성 에러
    @Test
    void report_shouldReturn400_whenReasonMapFail_byMoimingApiException() throws Exception {

        // given
        dataSu();
        ReportCreateReqDto requestDto = new ReportCreateReqDto(POST, moimPost.getId(), 100, false);
        String accessToken = createTestJwtToken(member, 2000);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_CREATE_REPORT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_MAPPABLE_ENUM_VALUE.getErrCode()));

        // then - db verify (그냥 저장됐는지만 확인하면 됨)
        em.flush();
        em.clear();

        List<Report> report = em.createQuery("SELECT r FROM Report r WHERE r.reporterId = :memberId", Report.class)
                .setParameter("memberId", member.getId())
                .getResultList();
        assertTrue(report.isEmpty());

    }
}
