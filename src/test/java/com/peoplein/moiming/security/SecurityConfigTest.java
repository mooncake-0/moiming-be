package com.peoplein.moiming.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 Security Config 가 제대로 설정되었는지
 MockMvc 를 활용하여 Test 한다
 */
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void security_shouldReturn401_whenUnauthenticated_byEntrypoint() throws Exception {

        //given
        //when
        ResultActions resultActions = mvc.perform(get("/any/other/than/auth"));

        //then
        resultActions.andExpect(status().isUnauthorized());

    }

    @Test
    void security_shouldPassAuthenticationFilterAndReturn404_whenAuthUrlPath() throws Exception{
        //given
        //when
        ResultActions resultActions = mvc.perform(get(API_SERVER + API_AUTH_VER + API_AUTH + "/notexistpath"));

        //then
        resultActions.andExpect(status().isNotFound());
    }


    // 권한 분리시 진행
    @Test
    void security_shouldReturn403_whenUnauthorized() throws Exception {

    }
}
