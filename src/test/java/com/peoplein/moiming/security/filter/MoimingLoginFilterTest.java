package com.peoplein.moiming.security.filter;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Assertions;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;

import static com.peoplein.moiming.NetworkSetting.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
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
    private final String LOGIN_URL = API_SERVER + API_AUTH_VER + API_AUTH + "/login";
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
        member = makeTestMember(memberEmail, memberPhone, memberName, role);

        roleRepository.save(role);
        memberRepository.save(member);

        em.flush();
        em.clear();
    }


    @Test
    void filter_should_login_when_right_info_passed() throws Exception{

        //given
        MemberLoginReqDto loginReqDto = new MemberLoginReqDto(memberEmail, password);
        String requestDto = om.writeValueAsString(loginReqDto);

        //when
        ResultActions resultActions = mvc.perform(post(LOGIN_URL).content(requestDto).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String jwtAccessToken = resultActions.andReturn().getResponse().getHeader(JwtParams.HEADER);

        //then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.memberEmail").value(memberEmail));
        assertNotNull(jwtAccessToken);
        assertThat(jwtAccessToken).startsWith(JwtParams.PREFIX);
        assertTrue(StringUtils.hasText(jwtAccessToken.replace(JwtParams.PREFIX, "")));
    }


    @Test
    void filter_should_return_500_when_wrong_dto_passed_by_extra_exception() throws Exception {

        // given
        Map<String, String> wrongDto = new HashMap<>();
        wrongDto.put("memberEmoll", memberEmail);
        wrongDto.put("password", password);
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(LOGIN_URL).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isInternalServerError());
    }


    @Test
    void filter_should_return_400_when_empty_param_passed_by_bad_input_exception() throws Exception {

        // given
        MemberLoginReqDto wrongDto = new MemberLoginReqDto(memberEmail, "");
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(LOGIN_URL).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
    }


    @Test
    void filter_should_return_401_when_email_not_found_by_username_not_found_exception() throws Exception {
        // given
        MemberLoginReqDto wrongDto = new MemberLoginReqDto("not@registered.com", "1234");
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(LOGIN_URL).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
    }


    @Test
    void filter_should_return_401_when_password_wrong_by_bad_credential_exception() throws Exception {
        // given
        MemberLoginReqDto wrongDto = new MemberLoginReqDto(memberEmail, password + "a");
        String requestDto = om.writeValueAsString(wrongDto);

        // when
        ResultActions resultActions = mvc.perform(post(LOGIN_URL).content(requestDto).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
    }


    @Test
    void filter_should_return_500_when_nothing_given_by_extra_exception() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(post(LOGIN_URL));

        // then
        resultActions.andExpect(status().isInternalServerError());
    }

}