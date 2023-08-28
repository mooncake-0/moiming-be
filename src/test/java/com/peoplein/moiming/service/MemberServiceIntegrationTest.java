package com.peoplein.moiming.service;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;


/*
 Integrated Test 는 통합 테스트로 ㄱㄱ
 Repository 와 분리 필요
 - Service 단위테스트만 진행
 */
@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberServiceIntegrationTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberService service;


//    @Test
    void test1() {

        // Given:
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim1 = TestUtils.createMoimOnly();
        Moim moim2 = TestUtils.createMoimOnly();

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
                schedule1,schedule2,schedule3,schedule4,schedule5,schedule6,schedule7,schedule8);



        // When:
        MemberService.MemberHomeDto memberHomeDto = service.serviceHome(member);

        // Then:
        assertThat(memberHomeDto.getMoimScheduleDto().getMoimScheduleMap().size()).isEqualTo(2);
        assertThat(memberHomeDto.getMoimScheduleDto().getMoimScheduleMap().get(moim1)).isEqualTo(schedule3);
        assertThat(memberHomeDto.getMoimScheduleDto().getMoimScheduleMap().get(moim2)).isEqualTo(schedule7);
        assertThat(memberHomeDto.getMoimNotices().size()).isEqualTo(0);
        assertThat(memberHomeDto.getMemberSchedule().size()).isEqualTo(5);
    }

    private void persist(Object ... objects) {
        Arrays.stream(objects).forEach(o -> em.persist(o));
    }


}