package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.model.dto.request.TokenReqDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestDto;
import com.peoplein.moiming.support.TestModelParams;
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
import org.testcontainers.shaded.com.github.dockerjava.core.exec.ExecCreateCmdExec;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.NetworkSetting.*;
import static com.peoplein.moiming.support.TestDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*


 - reissueToken - 잘못된 토큰 (이메일 추출은 가능) 일 시 Fail & RT 삭제는 Service 에서 확인

 */
@AutoConfigureMockMvc
@Transactional // Test Data 저장을 위함
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AuthControllerTest extends TestObjectCreator {

    private final String AUTH_URL = API_SERVER + API_AUTH_VER + API_AUTH;
    private final ObjectMapper om = new ObjectMapper();
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member registeredMember;

    private String savedToken;

    @BeforeEach
    void dataSu() {


        Role testRole = makeTestRole(RoleType.USER);
        registeredMember = makeTestMember("registered@mail.com", "01000000000", "등록된", testRole);
        savedToken = createTestJwtToken(registeredMember, 2000);
        registeredMember.changeRefreshToken(savedToken); // reissue 단 test 를 위해 Test token 을 주입해둔다

        roleRepository.save(testRole);
        memberRepository.save(registeredMember);

        em.flush();
        em.clear();

    }


    @Test
    void checkUidAvailable_should_return_200_when_available() throws Exception {

        // given
        String availableEmail = memberEmail;

        // when
        ResultActions resultActions = mvc.perform(get(AUTH_URL + "/available/" + availableEmail));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
    }


    @Test
    void checkUidAvailable_should_return_400_when_unavailable_by_moiming_api_exception() throws Exception {

        // given
        String unavailableEmail = "registered@mail.com";

        // when
        ResultActions resultActions = mvc.perform(get(AUTH_URL + "/available/" + unavailableEmail));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }


    @Test
    void checkUidAvailable_should_return_404_when_no_email_passed() throws Exception {

        // given
        String noEmail = "";

        // when
        ResultActions resultActions = mvc.perform(get(AUTH_URL + "/available/" + noEmail));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isNotFound());

    }


    @Test
    void signIn_should_return_member_dto_and_200_when_successful() throws Exception {

        // given
        TestMemberRequestDto reqDto = makeMemberReqDto(memberEmail, memberName, memberPhone);
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/signin").content(requestString).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String jwtValue = resultActions.andReturn().getResponse().getHeader(JwtParams.HEADER);
        String jwtAccessToken = jwtValue.replace(JwtParams.PREFIX, "");
        System.out.println("responseBody = " + responseBody);

        // then
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(jsonPath("$.data.nickname").exists());
        resultActions.andExpect(jsonPath("$.data.memberEmail").value(memberEmail));
        assertTrue(jwtValue.startsWith(JwtParams.PREFIX));
        assertTrue(StringUtils.hasText(jwtAccessToken));
    }


    // 중복 EMAIL 유저
    @Test
    void signIn_should_return_400_when_email_duplicates_by_moiming_api_exception() throws Exception {

        // given
        String unavailableEmail = "registered@mail.com";
        TestMemberRequestDto reqDto = makeMemberReqDto(unavailableEmail, memberName, memberPhone);
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/signin").content(requestString).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
    }


    // 중복 핸드폰 유저
    @Test
    void signIn_should_return_400_when_phone_duplicates_by_moiming_api_exception() throws Exception {

        // given
        String unavailablePhone = "01000000000";
        TestMemberRequestDto reqDto = makeMemberReqDto(memberEmail, memberName, unavailablePhone);
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/signin").content(requestString).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
    }


    // VALIDATION 에서 걸릴 때 (없는 값 하나 대표적으로)
    @Test
    void signIn_should_return_400_when_request_dto_validation_fails_by_moiming_validation_exception() throws Exception{

        // given
        TestMemberRequestDto requestDto = makeMemberReqDto(memberEmail, memberName, memberPhone);
        requestDto.setFcmToken(""); // 빈값 치환
        String requestString = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/signin").content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }


    // 이메일 형식 오류
    @Test
    void signIn_should_return_400_when_email_format_wrong_by_moiming_validation_exception() throws Exception {

        // given
        String wrongEmailFormat = "hellonaver.com";
        TestMemberRequestDto requestDto = makeMemberReqDto(wrongEmailFormat, memberName, memberPhone);
        String requestString = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/signin").content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));
    }



    // TODO :: 이메일 길이로 인한 형식 오류 및 비밀번호 형식 DTO 에 제대로 명시 후 Test
    // 비밀번호 형식 오류 TODO : 비밀번호 조건 제대로 설정 후 재 Test 필요


    @Test
    void signIn_should_return_400_when_password_condition_fails_by_moiming_validation_exception() throws Exception {

        // given
        TestMemberRequestDto requestDto = makeMemberReqDto(memberEmail, memberName, memberPhone);
        requestDto.setPassword("123");
        String requestString = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/signin").content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(-1));

    }
    //


    // 토큰 재발급
    @Test
    void reissueToken_should_return_new_token_and_200_when_successful() throws Exception {

        // given
        TokenReqDto reqDto = new TokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(savedToken); // 기존 토큰을 가지고 간다
        String requestBody = om.writeValueAsString(reqDto);


        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/token").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String headerJwt = resultActions.andReturn().getResponse().getHeader(JwtParams.HEADER);
        String reissuedToken = headerJwt.replace(JwtParams.PREFIX, "");
        System.out.println("responseBody = " + responseBody);
        System.out.println("headerJwt = " + headerJwt);

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.refreshToken").exists()); // Refresh Token 재발급
        assertTrue(headerJwt.startsWith(JwtParams.PREFIX));
        assertTrue(StringUtils.hasText(reissuedToken)); // Access Token 재발급

    }


    /*
     통합테스트 이므로, 추후 검증까지 진행해본다
     */
    @Test
    void reissueToken_should_save_new_token_to_requesting_member() throws Exception {

        // given
        TokenReqDto reqDto = new TokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(savedToken); // 기존 토큰을 가지고 간다
        String requestBody = om.writeValueAsString(reqDto);


        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/token").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        // then - member 조회 후 발급받은 Refresh Token 이 전달 받은것과 같은지 확인한다
        Member findMemberPs = memberRepository.findMemberById(registeredMember.getId());
        resultActions.andExpect(jsonPath("$.data.refreshToken").value(findMemberPs.getRefreshToken()));

    }

    // RT Expire 일 경우

    @Test
    void reissueToken_should_return_401_when_refresh_token_expired_by_token_expired_exception() throws Exception {

        // given
        TokenReqDto reqDto = new TokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(savedToken); // 기존 토큰을 가지고 간다

        String requestData = om.writeValueAsString(reqDto);
        Thread.sleep(3000); // 토큰을 만료시키자 (가끔 만료 안돼서 오류나서, 충분한 시간을 줌)


        // when
        ResultActions resultActions = mvc.perform(post(AUTH_URL + "/token").content(requestData).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(-100));
    }
}
