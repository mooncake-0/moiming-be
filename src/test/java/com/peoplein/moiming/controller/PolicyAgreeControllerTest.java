package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
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

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.PolicyAgreeUpdateReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PolicyAgreeControllerTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private PolicyAgreeRepository policyAgreeRepository;

    private Member testMember;


    @BeforeEach
    void be() {
        Role testRole = makeTestRole(RoleType.USER);
        testMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        em.persist(PolicyAgree.createPolicyAgree(testMember, SERVICE, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, PRIVACY, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, AGE, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, MARKETING_SMS, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, MARKETING_EMAIL, false));

        em.persist(testRole);
        em.persist(testMember);
        em.flush();
        em.clear();

    }


    // PolicyAgree 까지 저장된 Member 제공
    // 1개 성공 CASE
    // 2개 성공 CASE
    // AUTH 정보 없음 - 한번 해보자
    // policies 가 비어 있음
    // DTO 내부 Boolean 없음
    // DTO 내부 PolicyType 없음
    // DTO 내부 PolicyType 이 필수임
    // 같은 상태로의 변경 요청임


    // 성공
    @Test
    void updatePolicyAgree_shouldUpdateSinglePolicyAgree_whenSingleRightInfoPassed() throws Exception {

        // given
        Boolean[] hasAgreed = {false};
        PolicyType[] policyTypes = {MARKETING_SMS};
        List<PolicyAgreeDto> policyDtos = makePolicyUpdateReqDtoList(hasAgreed, policyTypes);
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(policyDtos);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));

        // then - db verify
        List<PolicyAgree> policyAgress = policyAgreeRepository.findByMemberId(testMember.getId());
        for (PolicyAgree policyAgree : policyAgress) {
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
        }
    }


    @Test
    void updatePolicyAgree_shouldUpdatePolicyAgrees_whenRightInfosPassed() throws Exception {

        // given
        Boolean[] hasAgreed = {false, true};
        PolicyType[] policyTypes = {MARKETING_SMS, MARKETING_EMAIL};
        List<PolicyAgreeDto> policyDtos = makePolicyUpdateReqDtoList(hasAgreed, policyTypes);
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(policyDtos);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));

        // then - db verify
        List<PolicyAgree> policyAgress = policyAgreeRepository.findByMemberId(testMember.getId());
        for (PolicyAgree policyAgree : policyAgress) {
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }

            if (policyAgree.getPolicyType().equals(MARKETING_EMAIL)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
        }
    }


    // 사실 필요 없는 테스트 401
    @Test
    void updatePolicyAgree_shouldReturn401_whenAccessTokenMissing_by() throws Exception {

        // given
        Boolean[] hasAgreed = {false, true};
        PolicyType[] policyTypes = {MARKETING_SMS, MARKETING_EMAIL};
        List<PolicyAgreeDto> policyDtos = makePolicyUpdateReqDtoList(hasAgreed, policyTypes);
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(policyDtos);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(-1));

        // then - db verify - 바뀌지 않음
        List<PolicyAgree> policyAgress = policyAgreeRepository.findByMemberId(testMember.getId());
        for (PolicyAgree policyAgree : policyAgress) {
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }

            if (policyAgree.getPolicyType().equals(MARKETING_EMAIL)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
        }
    }


    @Test
    void updatePolicyAgree_shouldReturn400_whenUpdatePolicyEmpty_byMoimingValidationException() throws Exception {

        // given
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseString = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseString = " + responseString);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }

    @Test
    void updatePolicyAgree_shouldReturn400_whenUpdatePolicyDtoBooleanNull_byMoimingValidationException() throws Exception {

        // given
        Boolean[] hasAgrees = {null, true}; // MARKETING EMAIL 을 바꿔보려는 시도 중, SMS 가 NULL 이 들어온 상황
        PolicyType[] policyTypes = {MARKETING_SMS, MARKETING_EMAIL};
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(makePolicyUpdateReqDtoList(hasAgrees, policyTypes));
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);
        System.out.println("requestBody = " + requestBody);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }


    @Test
    void updatePolicyAgree_shouldReturn400_whenUpdatePolicyDtoTypeNull_byMoimingValidationException() throws Exception {

        // given
        Boolean[] hasAgrees = {true, true}; // MARKETING EMAIL 을 바꿔보려는 시도 중, SMS 가 NULL 이 들어온 상황
        PolicyType[] policyTypes = {null, MARKETING_EMAIL};
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(makePolicyUpdateReqDtoList(hasAgrees, policyTypes));
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }


    @Test
    void updatePolicyAgree_shouldReturn400_whenUpdateEssentialTypePolicy_byMoimingApiException() throws Exception {

        // given
        Boolean[] hasAgrees = {false, false};
        PolicyType[] policyTypes = {SERVICE, MARKETING_EMAIL};
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(makePolicyUpdateReqDtoList(hasAgrees, policyTypes));
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }



    @Test
    void updatePolicyAgree_shouldReturn400_whenNoUpdateMadeToPolicyAgree_byMoimingApiException() throws Exception {

        // given
        Boolean[] hasAgrees = {true, true}; // MARKETING_EMAIL 은 이미 TRUE 임
        PolicyType[] policyTypes = {MARKETING_EMAIL, MARKETING_EMAIL};
        PolicyAgreeUpdateReqDto requestDto = new PolicyAgreeUpdateReqDto(makePolicyUpdateReqDtoList(hasAgrees, policyTypes));
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(testMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_POLICY_UPDATE).content(requestBody).contentType(MediaType.APPLICATION_JSON)
                .header(JwtParams.HEADER, JwtParams.PREFIX + accessToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }

}