package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Schedule;
import com.peoplein.moiming.repository.ScheduleRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.QSchedule.*;
import static com.peoplein.moiming.domain.QMoim.*;
import static com.peoplein.moiming.domain.QMemberScheduleLinker.*;

@Repository
@RequiredArgsConstructor
public class ScheduleJpaRepository implements ScheduleRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(Schedule schedule) {
        em.persist(schedule);
        return schedule.getId();
    }

    @Override
    public Schedule findById(Long scheduleId) {
        return em.find(Schedule.class, scheduleId);
    }

    @Override
    public Optional<Schedule> findOptionalById(Long scheduleId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(schedule)
                        .where(schedule.id.eq(scheduleId))
                        .fetchOne()
        );
    }

    @Override
    public Schedule findWithMoimById(Long scheduleId) {
        return queryFactory.selectFrom(schedule)
                .join(schedule.moim, moim).fetchJoin()
                .where(schedule.id.eq(scheduleId))
                .fetchOne();
    }

    @Override
    public Schedule findWithMemberScheduleLinkersById(Long scheduleId) {
        return queryFactory.selectFrom(schedule)
                .join(schedule.memberScheduleLinkers, memberScheduleLinker).fetchJoin()
                .where(schedule.id.eq(scheduleId))
                .fetchOne();
    }


    @Override
    public List<Schedule> findByMoimId(Long moimId) {

        /*
         Query : select s from Schedule s join fetch s.moim m
                    where m.moimId = :moimId
        */

        return queryFactory.selectFrom(schedule)
                .join(schedule.moim, moim).fetchJoin()
                .where(moim.id.eq(moimId))
                .fetch();

    }

    @Override
    public void removeAll(List<Long> scheduleIds) {

        queryFactory.delete(schedule).where(schedule.id.in(scheduleIds)).execute();
    }

    @Override
    public void remove(Schedule schedule) {
        em.remove(schedule);
    }

    @Override
    public List<Schedule> findAllSchedule() {
        return queryFactory.selectFrom(schedule)
                .fetch();
    }

}
