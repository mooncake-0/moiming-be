package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MemberMoimCounter;
import com.peoplein.moiming.domain.MemberMoimMaterializedView;
import com.peoplein.moiming.repository.MemberMoimMaterializedViewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberMoimCounterServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberMoimCounterService memberMoimCounterService;

    @Autowired
    MemberMoimMaterializedViewRepository materializedViewRepository;

    @Test
    void test1() {
        // Given
        final int size = 100;
        List<MemberMoimCounter> memberMoimCounters = IntStream.range(1, 1 + size)
                .mapToObj(value -> MemberMoimCounter.create(value + 100L, value + 1000L, LocalDate.now()))
                .collect(Collectors.toList());

        memberMoimCounters.forEach(memberMoimCounter -> em.persist(memberMoimCounter));

        // When
        memberMoimCounterService.update();

        // Then
        List<MemberMoimMaterializedView> result = em.createQuery("SELECT m FROM MemberMoimMaterializedView m", MemberMoimMaterializedView.class)
                .getResultList();

        assertThat(result.size()).isEqualTo(size);
    }

    @Test
    void test2() {
        // Given
        LocalDate date1 = LocalDate.of(2023, 9, 9);
        LocalDate date2 = LocalDate.of(2023, 9, 9);
        LocalDate date3 = LocalDate.of(2023, 9, 9);
        LocalDate date4 = LocalDate.of(2023, 9, 9);
        LocalDate date5 = LocalDate.of(2023, 9, 9);

        MemberMoimCounter m1 = MemberMoimCounter.create(1L, 1000L, date1);
        MemberMoimCounter m2 = MemberMoimCounter.create(1L, 1000L, date2);
        MemberMoimCounter m3 = MemberMoimCounter.create(1L, 1002L, date3);
        MemberMoimCounter m4 = MemberMoimCounter.create(1L, 1001L, date4);
        MemberMoimCounter m5 = MemberMoimCounter.create(1L, 1000L, date5);
        MemberMoimCounter m6 = MemberMoimCounter.create(2L, 1000L, date1);

        em.persist(m1);
        em.persist(m2);
        em.persist(m3);
        em.persist(m4);
        em.persist(m5);
        em.persist(m6);

        // When
        memberMoimCounterService.update();

        // Then
        List<MemberMoimMaterializedView> result = em.createQuery("SELECT m FROM MemberMoimMaterializedView m", MemberMoimMaterializedView.class)
                .getResultList();
        List<Long> resultList = result.stream().map(memberMoimMaterializedView -> memberMoimMaterializedView.getCount()).collect(Collectors.toList());

        assertThat(result.size()).isEqualTo(3);
        assertThat(resultList).containsExactly(4L, 1L, 1L);
    }

    @Test
    void test3() {
        // Given
        LocalDate date1 = LocalDate.of(2023, 9, 9);
        LocalDate date2 = LocalDate.of(2023, 9, 9);
        LocalDate date3 = LocalDate.of(2023, 9, 9);
        LocalDate date4 = LocalDate.of(2023, 9, 9);
        LocalDate date5 = LocalDate.of(2023, 9, 9);

        MemberMoimCounter m1 = MemberMoimCounter.create(1L, 1000L, date1);
        MemberMoimCounter m2 = MemberMoimCounter.create(1L, 1000L, date2);
        MemberMoimCounter m3 = MemberMoimCounter.create(1L, 1002L, date3);
        MemberMoimCounter m4 = MemberMoimCounter.create(1L, 1001L, date4);
        MemberMoimCounter m5 = MemberMoimCounter.create(1L, 1000L, date5);
        MemberMoimCounter m6 = MemberMoimCounter.create(2L, 1000L, date1);

        em.persist(m1);
        em.persist(m2);
        em.persist(m3);
        em.persist(m4);
        em.persist(m5);
        em.persist(m6);
        memberMoimCounterService.update();

        em.persist(MemberMoimCounter.create(10L, 1000L, LocalDate.of(2023,10,9)));
        em.persist(MemberMoimCounter.create(11L, 1000L, LocalDate.of(2023,10,9)));
        em.persist(MemberMoimCounter.create(12L, 1000L, LocalDate.of(2023,10,9)));
        em.persist(MemberMoimCounter.create(10L, 1001L, LocalDate.of(2023,10,9)));
        em.persist(MemberMoimCounter.create(11L, 1001L, LocalDate.of(2023,10,9)));
        em.persist(MemberMoimCounter.create(12L, 1001L, LocalDate.of(2023,10,9)));

        // When
        memberMoimCounterService.update();

        // Then
        List<MemberMoimMaterializedView> result = em.createQuery("SELECT m FROM MemberMoimMaterializedView m", MemberMoimMaterializedView.class)
                .getResultList();
        List<Long> resultList = result.stream().map(memberMoimMaterializedView -> memberMoimMaterializedView.getCount()).collect(Collectors.toList());

        assertThat(result.size()).isEqualTo(3);
        assertThat(resultList).containsExactly(7L, 4L, 1L);
    }

    @Test
    void test4() {
        // Given
        final int size = 100;
        List<MemberMoimCounter> memberMoimCounters = IntStream.range(1, 1 + size)
                .mapToObj(value -> MemberMoimCounter.create(value + 100L, value + 1000L, LocalDate.now()))
                .collect(Collectors.toList());

        memberMoimCounters.forEach(memberMoimCounter -> em.persist(memberMoimCounter));
        memberMoimCounterService.update();

        List<MemberMoimMaterializedView> before = em.createQuery("SELECT m FROM MemberMoimMaterializedView m", MemberMoimMaterializedView.class).getResultList();
        assertThat(before).isNotEmpty();

        // When
        memberMoimCounterService.deleteDoesNotExistMoim();

        // Then
        List<MemberMoimMaterializedView> mView = em.createQuery("SELECT m FROM MemberMoimMaterializedView m", MemberMoimMaterializedView.class).getResultList();
        List<MemberMoimCounter> counterList = em.createQuery("SELECT m FROM MemberMoimCounter m", MemberMoimCounter.class).getResultList();

        assertThat(mView.size()).isEqualTo(0);
        assertThat(counterList.size()).isEqualTo(0);
    }
}