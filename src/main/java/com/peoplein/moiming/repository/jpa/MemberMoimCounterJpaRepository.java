package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MemberMoimCounter;
import com.peoplein.moiming.repository.MemberMoimCounterRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.QMemberMoimCounter.*;

@Repository
@RequiredArgsConstructor
public class MemberMoimCounterJpaRepository implements MemberMoimCounterRepository {

    private static final int LOCK_TIMEOUT = 1;

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(MemberMoimCounter memberMoimCounter) {
        em.persist(memberMoimCounter);
    }

    @Override
    public Optional<MemberMoimCounter> findBy(Long memberId, Long moimId, LocalDate date) {
        MemberMoimCounter findInstance = query.select(memberMoimCounter)
                .where(memberMoimCounter.memberId.eq(memberId),
                        memberMoimCounter.moimId.eq(moimId),
                        memberMoimCounter.visitDate.eq(date)).fetchOne();

        return findInstance != null ?
                Optional.of(findInstance) :
                Optional.empty();
    }

    @Override
    public boolean acquireLock(String lockName) {
        final Query query = em.createNativeQuery("SELECT GET_LOCK(:lockName, :timeout)");
        query.setParameter("lockName", lockName);
        query.setParameter("timeout", LOCK_TIMEOUT);

        for (int i = 0; i < 10; i++) {
            BigInteger singleResult1 = (BigInteger) query.getSingleResult();
            int acquire = singleResult1.signum();
            if (acquire == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void releaseLock(String lockName) {
        final Query query = em.createNativeQuery("SELECT RELEASE_LOCK(:lockName)");
        query.setParameter("lockName", lockName);
    }

    @Override
    public List<MoimViewCountTuple> findByGroupByMoimId() {
        // MoimId + Counter
        return query.select(Projections.constructor(MoimViewCountTuple.class, memberMoimCounter.moimId, memberMoimCounter.count()))
                .from(memberMoimCounter)
                .groupBy(memberMoimCounter.moimId)
                .fetch();
    }

    @Override
    public void deleteByMoimIds(List<Long> moimIds) {
        query.delete(memberMoimCounter)
                .where(memberMoimCounter.moimId.in(moimIds))
                .execute();
    }
}