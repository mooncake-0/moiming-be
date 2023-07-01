package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberServiceIntegrationTest extends BaseTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberService service;



    @Test
    @DisplayName("기본적인 출력 검증")
    void serviceHomeTest1() {

        // Given:
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim1 = TestUtils.createMoimOnly();
        Moim moim2 = TestUtils.createMoimOnly();

        MemberMoimLinker memberMoimLinker1 = MemberMoimLinker.memberJoinMoim(member, moim1, MoimRoleType.CREATOR, MoimMemberState.ACTIVE);
        MemberMoimLinker memberMoimLinker2 = MemberMoimLinker.memberJoinMoim(member, moim2, MoimRoleType.CREATOR, MoimMemberState.ACTIVE);

        Schedule schedule1 = Schedule.createSchedule("a", "a", LocalDateTime.of(2023, 1, 1, 6, 30), 10, moim1, member);
        Schedule schedule2 = Schedule.createSchedule("b", "b", LocalDateTime.of(2022, 1, 1, 6, 30), 10, moim1, member);
        Schedule schedule3 = Schedule.createSchedule("c", "c", LocalDateTime.of(2027, 1, 1, 6, 30), 10, moim1, member);
        Schedule schedule4 = Schedule.createSchedule("d", "d", LocalDateTime.of(2024, 1, 1, 6, 30), 10, moim1, member);

        Schedule schedule5 = Schedule.createSchedule("A", "A", LocalDateTime.of(2023, 1, 1, 6, 30), 10, moim2, member);
        Schedule schedule6 = Schedule.createSchedule("B", "B", LocalDateTime.of(2022, 1, 1, 6, 30), 10, moim2, member);
        Schedule schedule7 = Schedule.createSchedule("C", "C", LocalDateTime.of(2027, 1, 1, 6, 30), 10, moim2, member);
        Schedule schedule8 = Schedule.createSchedule("D", "D", LocalDateTime.of(2024, 1, 1, 6, 30), 10, moim2, member);

        persist(member.getRoles().get(0).getRole(),
                member, moim1, moim2,
                memberMoimLinker1, memberMoimLinker2,
                schedule1,schedule2,schedule3,schedule4,schedule5,schedule6,schedule7,schedule8);

        forTest();

        // When:
        MemberService.MemberHomeDto memberHomeDto = service.serviceHome(member);

        // Then:
        assertThat(memberHomeDto.getMoimScheduleDto().getMoimScheduleMap().size()).isEqualTo(2);
        assertThat(memberHomeDto.getMoimScheduleDto().getMoimScheduleMap().get(moim1)).isEqualTo(schedule3);
        assertThat(memberHomeDto.getMoimScheduleDto().getMoimScheduleMap().get(moim2)).isEqualTo(schedule7);
        assertThat(memberHomeDto.getMoimNotices().size()).isEqualTo(0);
        assertThat(memberHomeDto.getMemberSchedule().size()).isEqualTo(5);
    }

    @Test
    @DisplayName("성공 : Notice 최근 3개만 노출되는 거 확인")
    void serviceHomeTest2() {

        // Given :
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim1 = TestUtils.createMoimOnly();
        Moim moim2 = TestUtils.createMoimOnly();

        MemberMoimLinker memberMoimLinker1 = MemberMoimLinker.memberJoinMoim(member, moim1, MoimRoleType.CREATOR, MoimMemberState.ACTIVE);
        MemberMoimLinker memberMoimLinker2 = MemberMoimLinker.memberJoinMoim(member, moim2, MoimRoleType.CREATOR, MoimMemberState.ACTIVE);

        MoimPost moim1Notice1 = MoimPost.createMoimPost("moim1-notice1", "content1", MoimPostCategory.NOTICE, true, false, moim1, member);
        MoimPost moim1Notice2 = MoimPost.createMoimPost("moim1-notice2", "content2", MoimPostCategory.NOTICE, true, false, moim1, member);
        MoimPost moim1Post1 = MoimPost.createMoimPost("post1", "content3", MoimPostCategory.REVIEW, false, false, moim1, member);
        MoimPost moim2Notice1 = MoimPost.createMoimPost("moim2-notice1", "content1", MoimPostCategory.NOTICE, true, false, moim2, member);
        MoimPost moim2Notice2 = MoimPost.createMoimPost("moim2-notice2", "content2", MoimPostCategory.NOTICE, true, false, moim2, member);
        MoimPost moim2Notice3 = MoimPost.createMoimPost("moim2-notice3", "content3", MoimPostCategory.NOTICE, true, false, moim2, member);

        persist(member.getRoles().get(0).getRole(),
                member,
                moim1, moim2,
                memberMoimLinker1,
                memberMoimLinker2,
                moim1Notice1, moim1Notice2, moim1Post1,
                moim2Notice1, moim2Notice2, moim2Notice3);

        forTest();

        // When :
        MemberService.MemberHomeDto memberHomeDto = service.serviceHome(member);

        // Then :
        assertThat(memberHomeDto.getMoimNotices().size()).isEqualTo(3);
        assertThat(memberHomeDto.getMoimNotices()).contains(moim2Notice1, moim2Notice2, moim2Notice3);
    }

    private void forTest() {
        em.flush();
        System.out.println("==== TEST QUERY START ====");
    }
    private void persist(Object ... objects) {
        Arrays.stream(objects).forEach(o -> em.persist(o));
    }


}