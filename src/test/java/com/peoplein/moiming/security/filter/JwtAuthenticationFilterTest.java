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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        member = makeTestMember(memberEmail, memberPhone, memberName, role);

        roleRepository.save(role);
        memberRepository.save(member);

        em.flush();
        em.clear();
    }


    @Test
    void filter_should_pass_jwt_authentication_filter_and_return_404() throws Exception {

        // given
        String sampleJwtToken = createTestJwtToken(member, 1000);

        // when
        ResultActions resultActions = mvc.perform(get("/any/other/path")
                .header(JwtParams.HEADER, JwtParams.PREFIX + sampleJwtToken));

        // then
        resultActions.andExpect(status().isNotFound()); // Authentication 정상 통과 후 404 반환이 정상
    }


    @Test
    void filter_should_return_401_when_token_expired() throws Exception {

        // given
        String sampleToken = createTestJwtToken(member, 1000);

        // when
        Thread.sleep(2000);
        ResultActions resultActions = mvc.perform(get("/api/v0/moim")
                .header(JwtParams.HEADER, JwtParams.PREFIX + sampleToken));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();

        System.out.println("responseBody = " + responseBody);
        resultActions.andExpect(status().isUnauthorized()); // TOKEN 만료시 TokenExpiredException 으로

    }
}