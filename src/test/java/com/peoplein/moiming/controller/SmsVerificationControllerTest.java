package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
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

import static com.peoplein.moiming.config.AppUrlPath.PATH_AUTH_REQ_SMS_VERIFY;
import static com.peoplein.moiming.domain.enums.VerificationType.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SmsVerificationControllerTest extends TestObjectCreator {

    // 성공 CASE 를 제외하고 실패 Case 들을 Test 함
    // 성공 CASE 는 실제 문자가 발생해야 하므로, API 테스트로 진행한다

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member;

    private final ObjectMapper om = new ObjectMapper();

    void suMember() {

        Role testRole = makeTestRole(RoleType.USER);
        member = makeTestMember(memberPhone, nickname, ci, testRole);

        em.persist(testRole);
        em.persist(member);

        em.flush();
        em.clear();
    }


    // 실패 - Validation - Type 없음, 번호 없음
    @Test
    void processSmsVerification_shouldReturn400_whenValidationFails_byMoimingValidationException() throws Exception {

        // given
        AuthSmsReqDto requestDto = new AuthSmsReqDto(null, "");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(2)));

    }


    // 실패 - 없는 멤버 요청
    @Test
    void processSmsVerification_shouldReturn404_whenFindIdButMemberNotFound_byMoimingApiException() throws Exception {

        // given
        AuthSmsReqDto requestDto = new AuthSmsReqDto(FIND_PW, memberPhone2);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_NOT_FOUND.getErrCode()));

    }

}
