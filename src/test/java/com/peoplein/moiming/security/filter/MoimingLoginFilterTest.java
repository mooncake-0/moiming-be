package com.peoplein.moiming.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
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
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimingLoginFilterTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();
    @Autowired
    private MockMvc mvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    @BeforeEach
    void be_input_user_db() {

        Role role = makeTestRole(RoleType.USER);
        member = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, role);

        roleRepository.save(role);
        memberRepository.save(member);

        em.flush();
        em.clear();
    }


    @Test
    void filter_shouldLogin_whenRightInfoPassed() throws Exception {

        //given
        AuthLoginReqDto loginReqDto = new AuthLoginReqDto(memberEmail, password);
        String requestDto = om.writeValueAsString(loginReqDto);

        //when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_LOGIN).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isOk());
        // then
        resultActions.andExpect(jsonPath("$.data.memberId").exists());
        resultActions.andExpect(jsonPath("$.data.nickname").exists());
        resultActions.andExpect(jsonPath("$.data.fcmToken").value(fcmToken));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());
        resultActions.andExpect(jsonPath("$.data.tokenInfo.accessToken").exists());
        resultActions.andExpect(jsonPath("$.data.tokenInfo.accessTokenExp").exists());
        resultActions.andExpect(jsonPath("$.data.tokenInfo.refreshToken").exists());
        resultActions.andExpect(jsonPath("$.data.tokenInfo.refreshTokenExp").exists());

        resultActions.andExpect(jsonPath("$.data.memberEmail").value(memberEmail));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberName").value(memberName));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberPhone").value(memberPhone));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberBirth").value(memberBirthStringFormat));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberGender").value(memberGender.toString()));
    }


    @Test
    void filter_shouldReturn400_whenWrongDtoPassed_byExtraException() throws Exception {

        // given
        Map<String, String> wrongDto = new HashMap<>();
        wrongDto.put("memberEmoll", memberEmail);
        wrongDto.put("password", password);
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_LOGIN).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_COMMON_INVALID_PARAM_NULL.getErrCode()));

    }


    @Test
    void filter_shouldReturn400_whenEmptyParamPassed_byLoginAttemptException() throws Exception {

        // given
        AuthLoginReqDto wrongDto = new AuthLoginReqDto(memberEmail, "");
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_LOGIN).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_LOGIN_REQUEST_INVALID.getErrCode()));
    }


    @Test
    void filter_shouldReturn404_whenEmailNotFound_byLoginAttemptException() throws Exception {

        // given
        AuthLoginReqDto wrongDto = new AuthLoginReqDto("not@registered.com", "1234");
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_LOGIN).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_LOGIN_EMAIL_NOT_FOUND.getErrCode()));
    }



    @Test
    void filter_shouldReturn401_whenPasswordWrong_byLoginAttemptException() throws Exception {

        // given
        AuthLoginReqDto wrongDto = new AuthLoginReqDto(memberEmail, password + "a");
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_LOGIN).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_LOGIN_PASSWORD_INCORRECT.getErrCode()));

    }


    @Test
    void filter_shouldReturn400_whenNothingGiven_byExtraException() throws Exception {

        // given
        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_LOGIN));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_COMMON_INVALID_PARAM_NULL.getErrCode()));

    }

}
