package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.support.TestModelParams.moimArea;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MoimMemberControllerTest {

    public final String MOIM_BASE_URL = API_SERVER + API_MOIM_VER + API_MOIM_MEMBER;
    public final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    // 실제 간이 저장 필요, Mock 이 아닌 Autowire 필요
    @Autowired
    private EntityManager em;


    @BeforeEach
    void be() { // 상황 SU 하기

    }

    // TEST 대상
    // getMoimMembers() - 모임의 모든 회원 및 상태 조회
    // 실패 CASE 작성

    // joinMoim() - 모임 가입하기
    // 실패 CASE 작성

    // leaveMoim() - 모임 나가기
    // 실패 CASE 작성

    // expelMember() - 모임원 강퇴하기
    // 실패 CASE 작성

    // grantMemberManager() - 모임원 운영진 임명
    // 실패 CASE 작성

}
