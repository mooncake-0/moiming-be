package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@AutoConfigureMockMvc
@Transactional // Test Data 저장을 위함
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MemberControllerTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Role testRole;
    private Member preMember, preMember2;

    void prepareMember() {

        Role testRole = makeTestRole(RoleType.USER);
        preMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        em.persist(testRole);
        em.persist(preMember);

        em.flush();
        em.clear();

    }


    void prepareMember2() {

        preMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        em.persist(preMember2);

        em.flush();
        em.clear();

    }


    // 멤버 단순 정보 조회
    @Test
    void getMember_shouldReturn200WithResponse_whenSuccessful() throws Exception {

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
        resultActions.andExpect(jsonPath("$.data.createdAt").isNotEmpty()); // TODO :: 그냥 Date 는 Persist 시점에 따라 0.00002 초 차이가 있기도 하다 -> 차라리 나가는 Format 을 정해놓은 뒤 Assertion 해는게 좋을 듯
        resultActions.andExpect(jsonPath("$.data.updatedAt").isNotEmpty()); //         그 전까진 그냥 존재 유무로 Test 한다
        resultActions.andExpect(jsonPath("$.data.lastLoginAt").isNotEmpty());//          LastLoginAt 은 이번에 업데이트 됨. preMember 는 영컨에 관리되지 않으므로 업데이트 안됨

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


    // DTO 빈 값
    @Test
    void confirmPassword_shouldReturn400_whenDtoWrong_byMoimingValidationException() throws Exception {

        // given
        prepareMember();
        MemberConfirmPwReqDto requestDto = new MemberConfirmPwReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MEMBER_CONFIRM_PW)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // 비밀번호 틀림
    @Test
    void confirmPassword_shouldReturn401_whenPwWrong_byMoimingApiException() throws Exception {

        // given
        prepareMember();
        MemberConfirmPwReqDto requestDto = new MemberConfirmPwReqDto(password + "wrong");
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_MEMBER_CONFIRM_PW)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_PW_INCORRECT.getErrCode()));

    }


    // 멤버 세부 조회 성공
    @Test
    void getMemberDetail_shouldReturn200WithResponse_whenSuccessful() throws Exception {

        // given
        prepareMember();
        String accessToken = createTestJwtToken(preMember, 2000);
        String[] before = {"memberId"};
        String[] after = {preMember.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(get(setParameter(PATH_MEMBER_GET_DETAIL_VIEW, before, after))
                .header(HEADER, PREFIX + accessToken));

        // then
        MemberInfo preMemberInfo = preMember.getMemberInfo();

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.memberId").value(preMember.getId()));
        resultActions.andExpect(jsonPath("$.data.memberEmail").value(preMember.getMemberEmail()));
        resultActions.andExpect(jsonPath("$.data.nickname").value(preMember.getNickname()));
        resultActions.andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.lastLoginAt").isNotEmpty());

        resultActions.andExpect(jsonPath("$.data.memberInfo.memberName").value(preMemberInfo.getMemberName()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberPhone").value(preMemberInfo.getMemberPhone()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberGender").value(preMemberInfo.getMemberGender().toString()));
        resultActions.andExpect(jsonPath("$.data.memberInfo.memberBirth").value(preMemberInfo.getMemberBirth() + ""));
        resultActions.andExpect(jsonPath("$.data.memberInfo.foreigner").value(preMemberInfo.isForeigner()));

    }


    // 닉네임 변경 성공
    @Test
    void changeNickname_shouldReturn200WithChangeNickname_whenSuccessful() throws Exception {

        // given
        prepareMember();
        String newNickname = nickname + "_new";
        MemberChangeNicknameReqDto requestDto = new MemberChangeNicknameReqDto(newNickname);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_NICKNAME)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.data.nickname").value(newNickname));

        // then - db verify
        em.flush();
        em.clear();

        Member member = em.find(Member.class, preMember.getId());
        assertThat(member.getNickname()).isEqualTo(newNickname);

    }


    // 실패 - 변화하지 않음까지 verify
    // DTO 실패
    @Test
    void changeNickname_shouldReturn400_whenDtoWrong_byMoimingValidationException() throws Exception {

        // given
        prepareMember();
        MemberChangeNicknameReqDto requestDto = new MemberChangeNicknameReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_NICKNAME)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1)));

    }


    // 현재와 동일한 닉네임
    @Test
    void changeNickname_shouldReturn400_whenCurrentNickname_byMoimingApiException() throws Exception {

        // given
        prepareMember();
        MemberChangeNicknameReqDto requestDto = new MemberChangeNicknameReqDto(nickname); // 같은 닉네임으로 변경 시도
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_NICKNAME)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_NICKNAME_UNAVAILABLE.getErrCode()));

        // then - db verify
        em.flush();
        em.clear();

        Member member = em.find(Member.class, preMember.getId());
        assertThat(member.getNickname()).isEqualTo(nickname);

    }


    // 누군가 사용중인 닉네임
    @Test
    void changeNickname_shouldReturn400_whenNicknameAlreadyInUse_byMoimingApiException() throws Exception {

        // given
        prepareMember();
        prepareMember2();
        MemberChangeNicknameReqDto requestDto = new MemberChangeNicknameReqDto(nickname2); // 이미 사용중인 닉네임으로 변경 시도
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_NICKNAME)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_NICKNAME_UNAVAILABLE.getErrCode()));

        // then - db verify
        em.flush();
        em.clear();

        Member member = em.find(Member.class, preMember.getId());
        assertThat(member.getNickname()).isEqualTo(nickname);

    }


    // 비밀번호 변경 성공 - 변화함까지 verify
    @Test
    void changePw_shouldReturn200_whenSuccessful() throws Exception {

        // given
        prepareMember();
        String newPw = password + "_new";
        MemberChangePwReqDto requestDto = new MemberChangePwReqDto(password, newPw);
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_PASSWORD)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(1));

        // then - db verify
        em.flush();
        em.clear();

        Member member = em.find(Member.class, preMember.getId());
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(newPw, member.getPassword())); // 비밀번호가 잘 바뀌었는지 확인한다
    }


    // 실패 - 변화하지 않음까지 verify
    // DTO 실패
    @Test
    void changePw_shouldReturn400_whenDtoWrong_byMoimingValidationException() throws Exception {

        // given
        prepareMember();
        MemberChangePwReqDto requestDto = new MemberChangePwReqDto();
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_PASSWORD)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));

    }


    // 현재 비밀번호 틀림
    @Test
    void changePw_shouldReturn401_whenPrePwWrong_byMoimingValidationException() throws Exception {

        // given
        prepareMember();
        MemberChangePwReqDto requestDto = new MemberChangePwReqDto(password + "_wrong", password + "_new");
        String requestBody = om.writeValueAsString(requestDto);
        String accessToken = createTestJwtToken(preMember, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_MEMBER_CHANGE_PASSWORD)
                .header(HEADER, PREFIX + accessToken)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_PW_INCORRECT.getErrCode()));

        // then - db verify
        em.flush();
        em.clear();

        Member member = em.find(Member.class, preMember.getId());
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(password, member.getPassword())); // 비밀번호는 바뀌지 않았음
    }


}
