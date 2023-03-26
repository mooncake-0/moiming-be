package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.session.MoimSession;
import com.peoplein.moiming.repository.MoimSessionRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.peoplein.moiming.domain.session.QMoimSession.*;

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
                .where(moimSession.id.eq(sessionId))
                .fetchOne());
    }
}
