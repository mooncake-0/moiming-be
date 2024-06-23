package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto;
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
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@Transactional // Test Data 저장을 위함
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PolicyAgreeControllerTest extends TestObjectCreator {


    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member;

    void dataSu() {

        Role testRole = makeTestRole(RoleType.USER);
        member = makeTestMember(memberPhone, nickname, ci, testRole);

        em.persist(testRole);
        em.persist(member);

        PolicyAgree policyAgree1 = PolicyAgree.createPolicyAgree(member, PolicyType.AGE, true);
        PolicyAgree policyAgree2 = PolicyAgree.createPolicyAgree(member, PolicyType.PRIVACY, true);
        PolicyAgree policyAgree3 = PolicyAgree.createPolicyAgree(member, PolicyType.SERVICE, true);
        PolicyAgree policyAgree4 = PolicyAgree.createPolicyAgree(member, PolicyType.MARKETING_AGREE, true);
        PolicyAgree policyAgree5 = PolicyAgree.createPolicyAgree(member, PolicyType.MARKETING_PUSH, false);
        PolicyAgree policyAgree6 = PolicyAgree.createPolicyAgree(member, PolicyType.MARKETING_SMS, true);

        em.persist(policyAgree1);
        em.persist(policyAgree2);
        em.persist(policyAgree3);
        em.persist(policyAgree4);
        em.persist(policyAgree5);
        em.persist(policyAgree6);

        em.flush();
        em.clear();
    }


    // 성공 - 선택 약관 전체 조회
    @Test
    void getPolicyAgree_shouldReturn200_whenDefaultReq() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MEMBER_POLICY_VIEW)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.policyAgrees").isArray());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'MARKETING_AGREE')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'MARKETING_PUSH')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'MARKETING_SMS')]").exists());

    }


    // 성공 - 필수 약관까지 모두 조회
    @Test
    void getPolicyAgree_shouldReturn200_whenOptionalFalseReq() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_MEMBER_POLICY_VIEW)
                .param("optionalOnly", "false")
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.policyAgrees").isArray());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'MARKETING_AGREE')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'MARKETING_PUSH')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'MARKETING_SMS')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'PRIVACY')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'SERVICE')]").exists());
        resultActions.andExpect(jsonPath("$.data.policyAgrees[?(@.policyType == 'AGE')]").exists());

    }


    // Exceptional Case 없음


    // 실패 - Validation
    @Test
    void updatePolicyAgree_shouldReturn400_whenNullRequestPassed_byMoimingValidationException() throws Exception {

        // given
        dataSu();
        PolicyAgreeUpdateReqDto requestData = new PolicyAgreeUpdateReqDto(null, null);
        String requestBody = om.writeValueAsString(requestData);
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(2)));

    }


    // 실패 - 필수 수정 시도
    @Test
    void updatePolicyAgree_shouldReturn400_whenMandatoryPolicyTypeUpdate_byMoimingValidationException() throws Exception {

        // given
        dataSu();
        PolicyAgreeUpdateReqDto requestData = new PolicyAgreeUpdateReqDto(true, PolicyType.AGE); // PUSH 를 True 로 변경
        String requestBody = om.writeValueAsString(requestData);
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

        // then
        em.flush();
        em.clear();

        // then - db verify - 변동 없음을 확인
        List<PolicyAgree> policyAgrees = em.createQuery("SELECT pa from PolicyAgree pa WHERE pa.member.id = :memberId", PolicyAgree.class)
                .setParameter("memberId", member.getId())
                .getResultList();

        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(PolicyType.AGE)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.SERVICE)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.PRIVACY)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.MARKETING_AGREE)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.MARKETING_PUSH)) {
                assertFalse(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.MARKETING_SMS)) {
                assertTrue(policyAgree.isHasAgreed());
            }
        }
    }


    // 약관 수정 - 선택 수정
    @Test
    void updatePolicyAgree_shouldReturn200_whenOptionalPolicyTypeUpdate() throws Exception {

        // given
        dataSu();
        PolicyAgreeUpdateReqDto requestData = new PolicyAgreeUpdateReqDto(true, PolicyType.MARKETING_PUSH); // PUSH 를 True 로 변경
        String requestBody = om.writeValueAsString(requestData);
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.hasAgreed").value(true));
        resultActions.andExpect(jsonPath("$.data.policyType").value(PolicyType.MARKETING_PUSH.toString()));

        // then
        em.flush();
        em.clear();

        // then - db verify
        List<PolicyAgree> policyAgrees = em.createQuery("SELECT pa from PolicyAgree pa WHERE pa.member.id = :memberId", PolicyAgree.class)
                .setParameter("memberId", member.getId())
                .getResultList();

        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(PolicyType.AGE)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.SERVICE)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.PRIVACY)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.MARKETING_AGREE)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.MARKETING_PUSH)) {
                assertTrue(policyAgree.isHasAgreed());
            }
            if (policyAgree.getPolicyType().equals(PolicyType.MARKETING_SMS)) {
                assertTrue(policyAgree.isHasAgreed());
            }
        }
    }

}
