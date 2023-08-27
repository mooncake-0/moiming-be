package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import com.peoplein.moiming.repository.MemberScheduleLinkerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


/*
 TODO TC::
 Test 해제
 Docker 미사용으로 기본적인 Test 환경 구축 우선
 - MSL Refactor 이후 재진행 예정
 */
@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberScheduleLinkerJpaRepositoryTest  {

    @Autowired
    EntityManager em;

    @Autowired
    MemberScheduleLinkerRepository memberScheduleLinkerRepository;

    Member creator;
    Moim moim1;
    Moim moim2;
    Moim moim3;

    Schedule schedule1;
    Schedule schedule2;
    Schedule schedule3;

    Schedule schedule4;
    Schedule schedule5;
    Schedule schedule6;

    Schedule schedule7;
    Schedule schedule8;


    @BeforeEach
    void setup() {

        creator = TestUtils.initMemberAndMemberInfo();

        moim1 = TestUtils.createMoimOnly("moim1");
        moim2 = TestUtils.createMoimOnly("moim2");
        moim3 = TestUtils.createMoimOnly("moim3");

        schedule1 = Schedule.createSchedule("title1", "location1",
                LocalDateTime.of(2023, 6, 1, 17, 30), 10,
                moim1, creator);

        schedule2 = Schedule.createSchedule("title2", "location2",
                LocalDateTime.of(2023, 6, 1, 18, 30), 10,
                moim2, creator);

        schedule3 = Schedule.createSchedule("title3", "location3",
                LocalDateTime.of(2023, 6, 1, 19, 30), 10,
                moim3, creator);

        schedule4 = Schedule.createSchedule("title3", "location3",
                LocalDateTime.of(2023, 6, 1, 20, 31), 10,
                moim3, creator);

        schedule5 = Schedule.createSchedule("title3", "location3",
                LocalDateTime.of(2023, 6, 1, 20, 32), 10,
                moim3, creator);

        schedule6 = Schedule.createSchedule("title3", "location3",
                LocalDateTime.of(2023, 6, 1, 20, 33), 10,
                moim3, creator);

        schedule7 = Schedule.createSchedule("title3", "location3",
                LocalDateTime.of(2023, 6, 1, 20, 34), 10,
                moim3, creator);

        schedule8 = Schedule.createSchedule("title3", "location3",
                LocalDateTime.of(2023, 6, 1, 20, 35), 10,
                moim3, creator);

        MemberScheduleLinker linker1 = MemberScheduleLinker.memberJoinSchedule(creator, schedule1, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker2 = MemberScheduleLinker.memberJoinSchedule(creator, schedule2, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker3 = MemberScheduleLinker.memberJoinSchedule(creator, schedule3, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker4 = MemberScheduleLinker.memberJoinSchedule(creator, schedule4, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker5 = MemberScheduleLinker.memberJoinSchedule(creator, schedule5, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker6 = MemberScheduleLinker.memberJoinSchedule(creator, schedule6, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker7 = MemberScheduleLinker.memberJoinSchedule(creator, schedule7, ScheduleMemberState.CREATOR);
        MemberScheduleLinker linker8 = MemberScheduleLinker.memberJoinSchedule(creator, schedule8, ScheduleMemberState.CREATOR);


        persist(creator.getRoles().get(0).getRole(),
                creator,
                moim1, moim2, moim3,
                schedule1, schedule2, schedule3, schedule4, schedule5, schedule6, schedule7, schedule8,
                linker1, linker2, linker3, linker4, linker5, linker6, linker7, linker8);
    }


//    @Test
    void findMemberScheduleLatest5ByMemberIdSuccessTest1() {
        // Given :
        Member member = TestUtils.initMemberAndMemberInfo("attendee", "attendee@moiming.net");
        persist(member);

        // When :
        List<MemberScheduleLinker> result = memberScheduleLinkerRepository.findMemberScheduleLatest5ByMemberId(member.getId());

        // Then :
        assertThat(result.size()).isEqualTo(0);
    }

//    @Test
    void findMemberScheduleLatest5ByMemberIdSuccessTest2() {
        // Given :
        Member member = TestUtils.initMemberAndMemberInfo("attendee", "attendee@moiming.net");

        MemberScheduleLinker linker1 = MemberScheduleLinker.memberJoinSchedule(member, schedule1, ScheduleMemberState.ATTEND);
        MemberScheduleLinker linker2 = MemberScheduleLinker.memberJoinSchedule(member, schedule2, ScheduleMemberState.ATTEND);
        MemberScheduleLinker linker3 = MemberScheduleLinker.memberJoinSchedule(member, schedule3, ScheduleMemberState.ATTEND);
        MemberScheduleLinker linker4 = MemberScheduleLinker.memberJoinSchedule(member, schedule4, ScheduleMemberState.ATTEND);
        MemberScheduleLinker linker5 = MemberScheduleLinker.memberJoinSchedule(member, schedule5, ScheduleMemberState.ATTEND);
        MemberScheduleLinker linker6 = MemberScheduleLinker.memberJoinSchedule(member, schedule6, ScheduleMemberState.NONATTEND);
        MemberScheduleLinker linker7 = MemberScheduleLinker.memberJoinSchedule(member, schedule7, ScheduleMemberState.NONATTEND);
        MemberScheduleLinker linker8 = MemberScheduleLinker.memberJoinSchedule(member, schedule8, ScheduleMemberState.NONATTEND);

        persist(member, linker1, linker2, linker3, linker4, linker5, linker6, linker7, linker8);

        // When :
        List<MemberScheduleLinker> result = memberScheduleLinkerRepository.findMemberScheduleLatest5ByMemberId(member.getId());

        // Then :
        assertThat(result.size()).isEqualTo(5);
        assertThat(result).extracting(MemberScheduleLinker::getId)
                .containsExactly(
                        linker8.getId(),
                        linker7.getId(),
                        linker6.getId(),
                        linker5.getId(),
                        linker4.getId());
    }

//    @Test
    void findMemberScheduleLatest5ByMemberIdSuccessTest3() {
        // Given :
        Member member = TestUtils.initMemberAndMemberInfo("attendee", "attendee@moiming.net");

        MemberScheduleLinker linker1 = MemberScheduleLinker.memberJoinSchedule(member, schedule1, ScheduleMemberState.ATTEND);
        MemberScheduleLinker linker2 = MemberScheduleLinker.memberJoinSchedule(member, schedule2, ScheduleMemberState.ATTEND);

        persist(member, linker1, linker2);

        // When :
        List<MemberScheduleLinker> result = memberScheduleLinkerRepository.findMemberScheduleLatest5ByMemberId(member.getId());

        // Then :
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(MemberScheduleLinker::getId)
                .containsExactly(
                        linker2.getId(),
                        linker1.getId());
    }



    private void persist(Object ... objects) {
        for (Object object : objects) {
            em.persist(object);
        }
        em.flush();
        em.clear();
    }
}