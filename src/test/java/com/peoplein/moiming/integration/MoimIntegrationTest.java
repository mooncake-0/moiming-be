package com.peoplein.moiming.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.peoplein.moiming.*;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.request.MoimRequestDto;
import com.peoplein.moiming.model.dto.response.MoimResponseDto;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.service.SecurityMemberService;
import com.peoplein.moiming.security.token.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MoimIntegrationTest extends BaseTest {

    @LocalServerPort
    private int port;
    private String url;

    private ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MockMvc mockMvc;

    private SecurityMember requestingSecurityMember;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    InitDatabaseQuery initDatabase;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void be() {
        // for test, Init DB
        TestUtils.truncateAllTable(jdbcTemplate);
        TestUtils.initDatabase(initDatabase);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        url = "http://localhost:8080" + NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM;
        String requestingMemberUidInTestDb = "wrock.kang";
        setRequestingMember(requestingMemberUidInTestDb);
    }

    private void setRequestingMember(String uid) {
        requestingSecurityMember = (SecurityMember) securityMemberService.loadUserByUsername(uid);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(requestingSecurityMember.getMember(), null, requestingSecurityMember.getAuthorities()));
    }

    @Autowired
    private SecurityMemberService securityMemberService;

    @Autowired
    private MemberMoimLinkerRepository memberMoimLinkerRepository;

    @Autowired
    private MoimRepository moimRepository;

    @Test
    @DisplayName("성공 @ /viewMemberMoim 유저의 모든 모임 조회")
    void 유저가_속한_모든_모임() throws Exception {

        //given
        // when
        url += "/viewMemberMoim";
        ResultActions resultActions = mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("data").isNotEmpty());
    }

    @Test
    @DisplayName("성공 @ /create 모임 생성")
    void 모임_생성() throws Exception {

        //given
        List<CategoryName> categoryNames = new ArrayList<>();
        categoryNames.add(CategoryName.STUDY);
        categoryNames.add(CategoryName.CODING);

        MoimRequestDto request = new MoimRequestDto(
                new MoimDto("예제 모임", "이제 모임을 시작할 때", "", new Area("이럴", "수가"), true)
                , new RuleJoinDto(2000, 1995, MemberGender.N, 3, false, true)
                , categoryNames
        );

        //when
        url += "/create";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
                        .characterEncoding("UTF-8"))
                .andDo(print()) // 콘솔에 요청 / 응답 출력
                .andExpect(status().isOk())
                .andReturn();

        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        MoimResponseDto moimResponseDto = om.convertValue(model.getData(), MoimResponseDto.class);

        Long moimId = moimResponseDto.getMoimDto().getMoimId();

        //then
        Moim moim = moimRepository.findById(moimId);

        assertNotNull(moim);
        assertEquals(1, moim.getCurMemberCount());
        assertEquals(requestingSecurityMember.getMember().getUid(), moim.getMemberMoimLinkers().get(0).getMember().getUid());
        assertEquals(requestingSecurityMember.getMember().getUid(), moim.getCreatedUid());

    }



}
