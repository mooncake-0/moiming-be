package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.Schedule;
import com.peoplein.moiming.repository.ScheduleRepository;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class ScheduleJpaRepositoryTest extends BaseTest {


    @Autowired
    EntityManager em;

    @Autowired
    ScheduleRepository scheduleRepository;



    @Test
    void test1() {

//        Moim moim = TestUtils.createMoimOnly();
//        Member member = TestUtils.initMemberAndMemberInfo();
//
//        em.persist(member.getRoles().get(0).getRole());
//        em.persist(member);
//        em.persist(moim);
//
//        Schedule schedule1 = Schedule.createSchedule("a", "a", LocalDateTime.of(2023, 1, 10, 6, 30), 10, moim, member);
//        Schedule schedule2 = Schedule.createSchedule("a", "a", LocalDateTime.of(2022, 1, 10, 6, 30), 10, moim, member);
//        Schedule schedule3 = Schedule.createSchedule("a", "a", LocalDateTime.of(2021, 1, 10, 6, 30), 10, moim, member);
//        Schedule schedule4 = Schedule.createSchedule("a", "a", LocalDateTime.of(2020, 1, 10, 6, 30), 10, moim, member);
//
//        em.persist(schedule1);
//        em.persist(schedule2);
//        em.persist(schedule3);
//        em.persist(schedule4);
//
//        em.flush();
//        em.clear();
//
//
//        List<Tuple> latestScheduleEachMoim = scheduleRepository.findLatestScheduleEachMoim();
//        for (Tuple tuple : latestScheduleEachMoim) {
//            System.out.println(tuple);
//        }


    }



}