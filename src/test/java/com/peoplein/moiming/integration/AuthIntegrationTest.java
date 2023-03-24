package com.peoplein.moiming.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.peoplein.moiming.*;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.auth.MemberLoginDto;
import com.peoplein.moiming.model.dto.auth.MemberSigninRequestDto;
import com.peoplein.moiming.model.dto.domain.MemberDto;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.JwtPropertySetting;
import com.peoplein.moiming.security.exception.AuthErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class AuthIntegrationTest extends BaseTest {

    @LocalServerPort
    private int port;
    private String url;
    private MockMvc mockMvc;

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

        url = "http://localhost:8080" + NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH;
    }

    private ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("성공 @ /signin")
    void 회원가입() throws Exception {

        //given
        MemberSigninRequestDto signinRequestDto = new MemberSigninRequestDto("jypark1234", "1234", "j@moiming.net");

        //when
        url += "/signin";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(signinRequestDto))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        MemberResponseDto memberResponseDto = om.convertValue(model.getData(), MemberResponseDto.class);
        Long memberId = memberResponseDto.getMemberDto().getId();
        String accessToken = mvcResult.getResponse().getHeader(JwtPropertySetting.HEADER_AT);
        String refreshToken = mvcResult.getResponse().getHeader(JwtPropertySetting.HEADER_RT);

        //then
        Member savedMember = memberRepository.findMemberAndMemberInfoById(memberId);

        assertEquals("jypark1234", savedMember.getUid());
        assertNotNull(accessToken);
        assertEquals(refreshToken, savedMember.getRefreshToken());
        assertEquals("j@moiming.net", savedMember.getMemberInfo().getMemberEmail());

    }

    @Test
    @DisplayName("실패 @ /signin - 중복 UID")
    void 회원가입_실패_중복_UID() throws Exception {

        //given
        MemberSigninRequestDto signinRequestDto = new MemberSigninRequestDto("wrock.kang", "1234", "kws8643@naver.com");

        //when
        url += "/signin";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(signinRequestDto))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andReturn();

        //then
        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        ErrorResponse errorResponse = om.convertValue(model.getData(), ErrorResponse.class);

        assertEquals(AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_UID.getErrorCode(), errorResponse.getErrorCode());
        assertEquals(AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_UID.getErrorType(), errorResponse.getErrorType());

    }

    @Test
    @DisplayName("실패 @ /signin - 중복 EMAIL")
    void 회원가입_실패_중복_EMAIL() throws Exception {

        //given
        MemberSigninRequestDto signinRequestDto = new MemberSigninRequestDto("jypark1234", "1234", "a@moiming.net");

        //when
        url += "/signin";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(signinRequestDto))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andReturn();

        //then
        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        ErrorResponse errorResponse = om.convertValue(model.getData(), ErrorResponse.class);

        assertEquals(AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_EMAIL.getErrorCode(), errorResponse.getErrorCode());
        assertEquals(AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_EMAIL.getErrorType(), errorResponse.getErrorType());

    }


    @Test
    @DisplayName("성공 @ /login")
    void 로그인() throws Exception {
        //given
        MemberLoginDto memberLoginDto = new MemberLoginDto("wrock.kang", "1234");

        //when
        url += "/login";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(memberLoginDto))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        MemberResponseDto memberResponseDto = om.convertValue(model.getData(), MemberResponseDto.class);

        MemberDto loggedInMemberDto = memberResponseDto.getMemberDto();
        String accessToken = mvcResult.getResponse().getHeader(JwtPropertySetting.HEADER_AT);
        String refreshToken = mvcResult.getResponse().getHeader(JwtPropertySetting.HEADER_RT);

        //then
        assertEquals("wrock.kang", loggedInMemberDto.getUid());
        assertNotNull(accessToken);
        assertNotNull(refreshToken); //
        assertEquals("a@moiming.net", memberResponseDto.getMemberInfoDto().getMemberEmail());
    }

    @Test
    @DisplayName("실패 @ /login - 잘못된 비밀번호")
    void 로그인_실패_비밀번호_불일치() throws Exception {

        //given
        MemberLoginDto memberLoginDto = new MemberLoginDto("wrock.kang", "1234567");

        //when
        url += "/login";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(memberLoginDto))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andReturn();

        //then
        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        ErrorResponse errorResponse = om.convertValue(model.getData(), ErrorResponse.class);

        assertEquals(AuthErrorEnum.AUTH_LOGIN_PW_ERROR.getErrorCode(), errorResponse.getErrorCode());
        assertEquals(AuthErrorEnum.AUTH_LOGIN_PW_ERROR.getErrorType(), errorResponse.getErrorType());

    }

    @Test
    @DisplayName("실패 @ /login - UID 없음")
    void 로그인_실패_UID_없음() throws Exception {
        //given
        MemberLoginDto memberLoginDto = new MemberLoginDto("non_existing_uid", "1234");

        //when
        url += "/login";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(memberLoginDto))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andReturn();

        //then
        ResponseModel model = TestHelper.convert(mvcResult, ResponseModel.class);
        ErrorResponse errorResponse = om.convertValue(model.getData(), ErrorResponse.class);

        assertEquals(AuthErrorEnum.AUTH_LOGIN_INVALID_INPUT.getErrorCode(), errorResponse.getErrorCode());
        assertEquals(AuthErrorEnum.AUTH_LOGIN_INVALID_INPUT.getErrorType(), errorResponse.getErrorType());

    }

}