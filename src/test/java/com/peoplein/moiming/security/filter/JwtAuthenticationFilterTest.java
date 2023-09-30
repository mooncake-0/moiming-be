package com.peoplein.moiming.security.filter;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestMockCreator;
import com.peoplein.moiming.support.TestModelParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 간단한 JWT Auth Filter 의 동작성을 검증
 추후 ADMIN 들어오면 ROLE 에 대한 동작성 추가 검증 필요
 */
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class JwtAuthenticationFilterTest extends TestObjectCreator {

    /*
     JWT 를 통한 인증 객체 생성 Filter Test
     */
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
        member = makeTestMember(memberEmail, memberPhone, memberName, nickname,  role);

        roleRepository.save(role);
        memberRepository.save(member);

        em.flush();
        em.clear();
    }


    @Test
    void filter_shouldPassAndReturn404() throws Exception {

        // given
        String sampleJwtToken = createTestJwtToken(member, 1000);

        // when
        ResultActions resultActions = mvc.perform(get("/any/other/path")
                .header(JwtParams.HEADER, JwtParams.PREFIX + sampleJwtToken));

        // then
        resultActions.andExpect(status().isNotFound()); // Authentication 정상 통과 후 404 반환이 정상
    }


    @Test
    void filter_shouldReturn401_whenTokenExpired_byTokenExpiredException() throws Exception {

        // given
        String sampleToken = createTestJwtToken(member, 1000);

        // when
        Thread.sleep(2000);
        ResultActions resultActions = mvc.perform(get("/any/other/path")
                .header(JwtParams.HEADER, JwtParams.PREFIX + sampleToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();

//        System.out.println("responseBody = " + responseBody);
        resultActions.andExpect(status().isUnauthorized()); // TOKEN 만료시 TokenExpiredException 으로
        resultActions.andExpect(jsonPath("$.code").value(-100)); // Token Expire 에 대한 code = -100

    }

    @Test
    void filter_shouldPassButReturn401_whenTokenEmpty_byEntrypoint() throws Exception{

        //given
        String wrongToken = "";

        //when
        ResultActions resultActions = mvc.perform(get("/any/other/path").header(JwtParams.HEADER, JwtParams.PREFIX + wrongToken));

        //then
        resultActions.andExpect(status().isUnauthorized());

    }

    @Test
    void filter_shouldReturn400_whenTokenInvalid_byJWTVerificationException() throws Exception{

        //given
        String wrongToken = "WRONG_TOKEN";

        //when
        ResultActions resultActions = mvc.perform(get("/any/other/path").header(JwtParams.HEADER, JwtParams.PREFIX + wrongToken));

        //then
        resultActions.andExpect(status().isBadRequest());

    }
}
