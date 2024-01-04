package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.temp.session.MoimSession;
import com.peoplein.moiming.repository.MoimSessionRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.session.QMoimSession.*;
import static com.peoplein.moiming.domain.QSchedule.*;

@Repository
@RequiredArgsConstructor
public class MoimSessionJpaRepository implements MoimSessionRepository {

    private final JPAQueryFactory queryFactory;

    private final EntityManager em;

    @Override
    public Long save(MoimSession moimSession) {
        em.persist(moimSession);
        return moimSession.getId();
    }

    @Override
    public Optional<MoimSession> findOptionalById(Long sessionId) {
        return Optional.ofNullable(queryFactory.selectFrom(moimSession)
                .innerJoin(moimSession.schedule, schedule).fetchJoin()
                .where(moimSession.id.eq(sessionId))
                .fetchOne());
    }

    @Override
    public List<MoimSession> findAllByMoimId(Long moimId) {
        return queryFactory.selectFrom(moimSession)
                .where(moimSession.moim.id.eq(moimId))
                .fetch();
    }

    @Override
    public void remove(MoimSession moimSession) {
        em.remove(moimSession);
    }
}
