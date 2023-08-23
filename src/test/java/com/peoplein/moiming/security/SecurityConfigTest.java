package com.peoplein.moiming.security;

import com.peoplein.moiming.NetworkSetting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.peoplein.moiming.NetworkSetting.*;
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
    void security_should_return_401_when_unauthenticated_by_entrypoint() throws Exception {

        //given
        //when
        ResultActions resultActions = mvc.perform(get("/any/other/than/auth"));

        //then
        resultActions.andExpect(status().isUnauthorized());

    }

    @Test
    void security_should_pass_authentication_filter_and_return_404_when_auth_path() throws Exception{
        //given
        //when
        ResultActions resultActions = mvc.perform(get(API_SERVER + API_AUTH_VER + API_AUTH + "/notexistpath"));

        //then
        resultActions.andExpect(status().isNotFound());
    }


    // 권한 분리시 진행
    @Test
    void security_should_return_403_when_unauthorized() throws Exception {

    }
}
