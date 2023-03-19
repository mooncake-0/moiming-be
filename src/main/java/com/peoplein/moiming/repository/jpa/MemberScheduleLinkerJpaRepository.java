package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MemberScheduleLinker;
import com.peoplein.moiming.repository.MemberScheduleLinkerRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.peoplein.moiming.domain.QMemberScheduleLinker.*;
import static com.peoplein.moiming.domain.QSchedule.*;

@Repository
@RequiredArgsConstructor
public class MemberScheduleLinkerJpaRepository implements MemberScheduleLinkerRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(MemberScheduleLinker memberScheduleLinker) {
        em.persist(memberScheduleLinker);
        return memberScheduleLinker.getId();
    }

    @Override
    public MemberScheduleLinker findByMemberAndScheduleId(Long memberId, Long scheduleId) {
        return queryFactory.selectFrom(memberScheduleLinker)
                .where(memberScheduleLinker.member.id.eq(memberId),
                        memberScheduleLinker.schedule.id.eq(scheduleId))
                .fetchOne();
    }

    @Override
    public MemberScheduleLinker findWithScheduleByMemberAndScheduleId(Long memberId, Long scheduleId) {
        return queryFactory.selectFrom(memberScheduleLinker)
                .join(memberScheduleLinker.schedule, schedule).fetchJoin()
                .where(memberScheduleLinker.member.id.eq(memberId),
                        memberScheduleLinker.schedule.id.eq(scheduleId))
                .fetchOne();
    }

    @Override
    public void remove(MemberScheduleLinker memberScheduleLinker) {
        em.remove(memberScheduleLinker);
    }

    @Override
    public void removeAllByScheduleId(Long scheduleId) {
        queryFactory.delete(memberScheduleLinker).where(memberScheduleLinker.schedule.id.eq(scheduleId)).execute();
    }

    @Override
    public void removeAllByScheduleIds(List<Long> scheduleIds) {
        queryFactory.delete(memberScheduleLinker).where(memberScheduleLinker.schedule.id.in(scheduleIds)).execute();
    }
}
