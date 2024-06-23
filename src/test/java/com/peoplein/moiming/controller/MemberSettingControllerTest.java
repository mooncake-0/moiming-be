package com.peoplein.moiming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberSetting;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.model.dto.request.MemberSettingReqDto;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MemberSettingReqDto.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MemberSettingControllerTest extends TestObjectCreator {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member;
    private MemberSetting memberSetting;

    void dataSu() {

        Role testRole = makeTestRole(RoleType.USER);
        member = makeTestMember(memberPhone, nickname, ci, testRole);
        memberSetting = MemberSetting.saveMemberSetting(member);

        em.persist(testRole);
        em.persist(member);
        em.persist(memberSetting);

        em.flush();
        em.clear();
    }


    // 유저 설정 조회 성공
    @Test
    void getMemberNotifySetting_shouldReturn200_whenRightInfoPassed() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(get(PATH_GET_MEMBER_NOTIFY_SETTING)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.allowNoticeNotify").value(false));
        resultActions.andExpect(jsonPath("$.data.allowMoimNotify").value(false));

    }


    // No Exceptional Case


    // 유저 설정 수정 성공 : Moim Notify 수정 시도
    @Test
    void updateMemberNotifySetting_shouldReturn200_whenMoimNotifyChangeTrial() throws Exception {

        // given
        dataSu();
        MemberSettingUpdateReqDto requestData = new MemberSettingUpdateReqDto(false, true);
        String requestBody = om.writeValueAsString(requestData);
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_UPDATE_MEMBER_NOTIFY_SETTING)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.allowNoticeNotify").value(false));
        resultActions.andExpect(jsonPath("$.data.allowMoimNotify").value(true));

    }


    // 유저 설정 수정 성공 : Notice Notify 수정 시도
    @Test
    void updateMemberNotifySetting_shouldReturn200_whenNoticeNotifyChangeTrial() throws Exception {

        // given
        dataSu();
        MemberSettingUpdateReqDto requestData = new MemberSettingUpdateReqDto(true, false);
        String requestBody = om.writeValueAsString(requestData);
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_UPDATE_MEMBER_NOTIFY_SETTING)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.allowNoticeNotify").value(true));
        resultActions.andExpect(jsonPath("$.data.allowMoimNotify").value(false));

    }


    // Request Body Validation Exception
    @Test
    void updateMemberNotifySetting_shouldReturn400_whenValidationFail_byMoimingValidationException() throws Exception {

        // given
        dataSu();
        MemberSettingUpdateReqDto requestData = new MemberSettingUpdateReqDto(null, null);
        String requestBody = om.writeValueAsString(requestData);
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_UPDATE_MEMBER_NOTIFY_SETTING)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(ExceptionValue.COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(2)));

    }

}
