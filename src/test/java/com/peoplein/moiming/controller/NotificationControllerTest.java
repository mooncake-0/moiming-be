package com.peoplein.moiming.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.MoimMemberRoleType.NORMAL;
import static com.peoplein.moiming.domain.enums.MoimMemberState.ACTIVE;
import static com.peoplein.moiming.domain.enums.NotificationSubCategory.*;
import static com.peoplein.moiming.domain.enums.NotificationTopCategory.*;
import static com.peoplein.moiming.domain.enums.NotificationType.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.security.token.JwtParams.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class NotificationControllerTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    private Member member, member2;
    private Moim moim, moim2, moim3;
    private Notification notification1_2, notification3_2; // PAGING Test 를 위한 Global 변수

    // Data 예정 사항
    // Member 1, Member 2, Member 3 이 있음
    // Member 1,2,3 가 운영중인 모임이 각각 1개 있음, Member1 은 Moim2, Moim3 의 모임원
    // 모임1에서 알림 3개 발생
    // 모임2에서 알림 3개 발생
    // 모임3에서 알림 2개 발생
    void dataSu() {

        Role role = makeTestRole(RoleType.USER);
        member = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, role);
        member2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, role);
        Member member3 = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, role);
        em.persist(role);
        em.persist(member);
        em.persist(member2);
        em.persist(member3);

        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth2SampleCategory), 1, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        moim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), member);
        moim2 = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), member2);
        moim3 = makeTestMoim(moimName2, maxMember2, moimArea2.getState(), moimArea2.getCity(), List.of(testCategory1, testCategory1_1), member3);

        MoimMember.memberJoinMoim(member, moim2, NORMAL, ACTIVE);
        MoimMember.memberJoinMoim(member, moim3, NORMAL, ACTIVE);

        em.persist(moim);
        em.persist(moim2);
        em.persist(moim3);

        // -- 알림 생성
        // ORDER BY Test 를 위함
        Notification notification1_1 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림1_1", moim.getId(), null);
        Notification notification1_3 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림1_3", moim.getId(), 100L);
        notification1_2 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림1_2", moim.getId(), null);

        Notification notification2_1 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림2_1", moim2.getId(), null);
        Notification notification2_2 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림2_2", moim2.getId(), null);
        Notification notification2_3 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림2_3", moim2.getId(), 110L);

        notification3_2 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림3_2", moim3.getId(), 120L);
        Notification notification3_1 = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member.getId(), "", "알림3_1", moim3.getId(), null);

        em.persist(notification1_1);
        em.persist(notification1_3);
        em.persist(notification1_2);
        em.persist(notification2_1);
        em.persist(notification2_2);
        em.persist(notification2_3);
        em.persist(notification3_2);
        em.persist(notification3_1);

        em.flush();
        em.clear();

    }


    // Test 예정 사항
    // 실패 - topCategory Query Param 없음
    @Test
    void getMemberNotification_shouldReturn400WithResponse_whenTopCategoryNotExist_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isBadRequest());
        resultAction.andExpect(jsonPath("$.code").value(COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 실패 - topCategory Query Param 잘못됨
    @Test
    void getMemberNotification_shouldReturn422WithResponse_whenTopCategoryNotTransferred_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", "잘못된카테고리")
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isUnprocessableEntity());
        resultAction.andExpect(jsonPath("$.code").value(COMMON_INVALID_SITUATION.getErrCode()));

    }


    // 실패 - moimType 잘못됨 (없어도는 되는데, manage / join 아니면 안됨)
    @Test
    void getMemberNotification_shouldReturn400WithResponse_whenMoimTypeWrong_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("moimType", "hello") // 잘못됨
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isBadRequest());
        resultAction.andExpect(jsonPath("$.code").value(COMMON_INVALID_REQUEST_PARAM.getErrCode()));

    }


    // 실패 - lastNotification 찾을 수 없음
    @Test
    void getMemberNotification_shouldReturn404WithResponse_whenLastNotificationNotFound_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);
        Long wrongNotiId = 1234L;

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("lastNotificationId", wrongNotiId + "")
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isNotFound());
        resultAction.andExpect(jsonPath("$.code").value(MEMBER_NOTIFICATION_NOT_FOUND.getErrCode()));

    }


    // 이하로 topCategory = 모임 고정, limit 는 자율
    // moimType = 없음, lastNotificationId = 없음 > 종류 상관 없이 모든 알림 다 가져옴
    @Test
    void getMemberNotification_shouldReturn200WithResponse_whenNoMoimTypeNoLni() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isOk());
        resultAction.andExpect(jsonPath("$.data").isArray());
        resultAction.andExpect(jsonPath("$.data", hasSize(8)));
        checkSorted(resultAction.andReturn().getResponse().getContentAsString());

    }


    // moimType = join, lNI 없음 > 내가 가입된 모임의 알림을 가져옴
    @Test
    void getMemberNotification_shouldReturn200WithResponse_whenMoimTypeJoinNoLni() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("moimType", "join")
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isOk());
        resultAction.andExpect(jsonPath("$.data").isArray());
        resultAction.andExpect(jsonPath("$.data", hasSize(5)));
        checkSorted(resultAction.andReturn().getResponse().getContentAsString());

    }


    // 알림2_1 ~ 알림3_2, 알림3_1 순으로 생성되었으므로, 역으로 전송된다
    // 알림 3_2 에서 컷 되었다 가정
    // moimType = join, lNI 있음 > 커서 페이징 테스트
    @Test
    void getMemberNotification_shouldReturn200WithResponse_whenMoimTypeJoinLniNoti2_3() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("moimType", "join")
                .param("lastNotificationId", notification3_2.getId() + "")
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isOk());
        resultAction.andExpect(jsonPath("$.data").isArray());
        resultAction.andExpect(jsonPath("$.data", hasSize(3)));
        resultAction.andExpect(jsonPath("$.data[*].topCategoryId", everyItem(is(moim2.getId().intValue())))); // 따라서 다음 알림들은 모두 모임 2의 알림들이다) / 테스트 종속적인 ASSERTION
        checkSorted(resultAction.andReturn().getResponse().getContentAsString());

    }


    // moimType = manage, lNI 없음 > 내가 운영중인 모임의 알림을 가져옴
    @Test
    void getMemberNotification_shouldReturn200WithResponse_whenMoimTypeManageNoLni() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("moimType", "manage")
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isOk());
        resultAction.andExpect(jsonPath("$.data").isArray());
        resultAction.andExpect(jsonPath("$.data", hasSize(3)));
        resultAction.andExpect(jsonPath("$.data[*].topCategoryId", everyItem(is(moim.getId().intValue()))));
        checkSorted(resultAction.andReturn().getResponse().getContentAsString());

    }


    // moimType = manage, lNI 있음 > 커서 페이징 테스트
    // 알림이 noti1_1, 1_3, 1_2 순으로 생성되었으므로, 역순으로 온다. 1_2 가 마지막이라고 가정
    @Test
    void getMemberNotification_shouldReturn200WithResponse_whenMoimTypeManageLniNoti1_2() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);

        // when
        ResultActions resultAction = mvc.perform(get(PATH_MEMBER_NOTIFICATIONS)
                .header(HEADER, PREFIX + accessToken)
                .param("topCategory", MOIM.getValue())
                .param("moimType", "manage")
                .param("lastNotificationId", notification1_2.getId() + "")
                .param("limit", "20"));

        // then
        resultAction.andExpect(status().isOk());
        resultAction.andExpect(jsonPath("$.data").isArray());
        resultAction.andExpect(jsonPath("$.data", hasSize(2)));
        resultAction.andExpect(jsonPath("$.data[*].topCategoryId", everyItem(is(moim.getId().intValue())))); // 따라서 다음 알림들은 모두 모임 2의 알림들이다) / 테스트 종속적인 ASSERTION
        checkSorted(resultAction.andReturn().getResponse().getContentAsString());

    }


    void checkSorted(String jsonResp) throws JsonProcessingException {
        JsonNode jsonNode = om.readTree(jsonResp);
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("data");

        for (int i = 0; i < arrayNode.size() - 1; i++) {
            JsonNode node = arrayNode.get(i);
            JsonNode nextNode = arrayNode.get(i + 1);
            LocalDateTime nodeTime = LocalDateTime.parse(node.get("createdAt").asText());
            LocalDateTime nextNodeTime = LocalDateTime.parse(nextNode.get("createdAt").asText());
            assertTrue(nodeTime.isAfter(nextNodeTime) || nodeTime.isEqual(nextNodeTime)); // 같거나 빠른 날짜 순서대로 정렬된다
        }
    }


    // 정상 삭제
    @Test
    void deleteNotification_shouldReturn200AndDelete_whenDeleteNotification1_2Passed() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);
        String[] before = {"notificationId"};
        String[] after = {notification1_2.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MEMBER_NOTIFICATION_DELETE, before, after))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        Notification notification = em.find(Notification.class, notification1_2.getId());
        assertNull(notification);

    }


    // 실패 - Notification NOT FOUND
    @Test
    void deleteNotification_shouldReturn404_whenTargetNotificationNotFound_byMoimingApiException() throws Exception {

        // given
        dataSu();
        String accessToken = createTestJwtToken(member, 2000);
        String[] before = {"notificationId"};
        String[] after = {1234L + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MEMBER_NOTIFICATION_DELETE, before, after))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_NOTIFICATION_NOT_FOUND.getErrCode()));

        // then - db verify
        Notification notification = em.find(Notification.class, notification1_2.getId());
        assertNotNull(notification);
    }


    // 실패 - Notification NOT requestedMember's
    @Test
    void deleteNotification_shouldReturn403_whenTargetNotificationNotBelongToReqMember_byMoimingApiException() throws Exception {

        // given - su
        dataSu();
        Notification sampleNoti = Notification.createNotification(MOIM, COMMENT_CREATE, INFORM, member2.getId(), "", "Member2의 알림", moim.getId(), null);
        em.persist(sampleNoti);
        em.flush();
        em.clear();

        // given
        String accessToken = createTestJwtToken(member, 2000);
        String[] before = {"notificationId"};
        String[] after = {sampleNoti.getId() + ""};

        // when
        ResultActions resultActions = mvc.perform(delete(setParameter(PATH_MEMBER_NOTIFICATION_DELETE, before, after))
                .header(HEADER, PREFIX + accessToken));

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_NOT_AUTHORIZED.getErrCode()));

        // then - db verify
        Notification notification = em.find(Notification.class, notification1_2.getId());
        assertNotNull(notification);
    }
}


