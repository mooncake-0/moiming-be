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
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;
import static com.peoplein.moiming.support.TestModelParams.*;
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
        member = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        em.persist(testRole);
        em.persist(member);

        em.flush();
        em.clear();
    }


    // 실패 - Validation - Type 없음, 번호 없음
    @Test
    void processSmsVerification_shouldReturn400_whenValidationFails_byMoimingValidationException() throws Exception {

        // given
        AuthSmsReqDto requestDto = new AuthSmsReqDto(null, "", "", "");
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


    // 실패 - Validation - Type ID 인데 이름 없음
    @Test
    void processSmsVerification_shouldReturn400_whenReqTypeIdButNoName_byMoimingApiException() throws Exception {

        // given
        AuthSmsReqDto requestDto = new AuthSmsReqDto(FIND_ID, "", memberEmail, memberPhone);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_PARAM.getErrCode()));

    }


    // 실패 - Validation - Type PW 인데 이메일 없음
    @Test
    void processSmsVerification_shouldReturn400_whenReqTypePWButNoEmail_byMoimingApiException() throws Exception {

        // given
        AuthSmsReqDto requestDto = new AuthSmsReqDto(FIND_PW, memberName, "", memberPhone);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_INVALID_PARAM.getErrCode()));

    }


    // 실패 - 없는 멤버 요청
    @Test
    void processSmsVerification_shouldReturn404_whenFindIdButMemberNotFound_byMoimingApiException() throws Exception {

        // given
        AuthSmsReqDto requestDto = new AuthSmsReqDto(FIND_ID, memberName, "", memberPhone2);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_NOT_FOUND.getErrCode()));

    }


    // 실패 - FIND ID : 존재하는 멤버와 이름 불일치
    @Test
    void processSmsVerification_shouldReturn422_whenFindIdMemberAndReqNameNotMatch_byMoimingApiException() throws Exception {

        // given
        suMember();
        AuthSmsReqDto requestDto = new AuthSmsReqDto(FIND_ID, memberName2, "", memberPhone);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_INVALID_NAME_WITH_PHONE.getErrCode()));

    }


    // 실패 - FIND PW : 존재하는 멤버와 이메일 불일치
    @Test
    void processSmsVerification_shouldReturn422_whenFindPwMemberAndReqEmailNotMatch_byMoimingApiException() throws Exception {

        // given
        suMember();
        AuthSmsReqDto requestDto = new AuthSmsReqDto(FIND_PW, "", memberEmail2, memberPhone);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REQ_SMS_VERIFY)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_INVALID_NAME_WITH_EMAIL.getErrCode()));

    }

}
