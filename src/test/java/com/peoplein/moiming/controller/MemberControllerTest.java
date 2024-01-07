package com.peoplein.moiming.controller;


import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestModelParams;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional // Test Data 저장을 위함
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MemberControllerTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member preMember;

    void prepareMember() {

        Role testRole = makeTestRole(RoleType.USER);
        preMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        em.persist(testRole);
        em.persist(preMember);

        em.flush();
        em.clear();

    }


    // 멤버 단순 정보 조회
    @Test
    void getMember_shouldReturn200WithResponse_whenSuccessful() throws Exception{

        // given
        prepareMember();
        String accessToken = createTestJwtToken(preMember, 2000);
        String[] before = {"memberId"};
        String[] after = {preMember.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MEMBER_GET_VIEW, before, after))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.memberId").value(preMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberEmail").value(preMember.getMemberEmail()));
        resultActions.andExpect(jsonPath("$.data.nickname").value(preMember.getNickname()));
        resultActions.andExpect(jsonPath("$.data.createdAt").value(preMember.getCreatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.updatedAt").value(preMember.getUpdatedAt() + ""));
        resultActions.andExpect(jsonPath("$.data.lastLoginAt").isNotEmpty());// LastLoginAt 은 이번에 업데이트 됨. preMember 는 영컨에 관리되지 않으므로 업데이트 안됨

    }


    // 비밀번호 확인 요청
    @Test
    void confirmPassword_shouldReturn200_whenConfirmSuccess() throws Exception {

        // given
        prepareMember();
        MemberConfirmPwReqDto requestDto = new MemberConfirmPwReqDto(password);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MEMBER_CONFIRM_PW)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));

    }
}
