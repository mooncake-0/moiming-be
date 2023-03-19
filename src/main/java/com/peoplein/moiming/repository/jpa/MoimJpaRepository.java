package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.QMoim;
import com.peoplein.moiming.repository.MoimRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.Optional;

import static com.peoplein.moiming.domain.QMoim.*;
import static com.peoplein.moiming.domain.rules.QMoimRule.*;

@Repository
@RequiredArgsConstructor
public class MoimJpaRepository implements MoimRepository {


    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(Moim moim) {

        em.persist(moim);
        return moim.getId();
    }

    @Override
    public Moim findById(Long moimId) {
        return queryFactory.selectFrom(moim)
                .where(moim.id.eq(moimId))
                .fetchOne();
    }

    @Override
    public Optional<Moim> findOptionalById(Long moimId) {
        Moim moim = queryFactory.selectFrom(QMoim.moim)
                .where(QMoim.moim.id.eq(moimId))
                .fetchOne();
        return Optional.ofNullable(moim);
    }

    @Override
    public Moim findWithRulesById(Long moimId) {
        /*
         Query : select m from Moim m
                    join fetch m.moimRules mr
                    where m.id = :moimId;
        */

        return queryFactory.selectFrom(moim)
                .join(moim.moimRules, moimRule).fetchJoin()
                .where(moim.id.eq(moimId))
                .fetchOne();
    }

    @Override
    public void
    remove(Moim moim) {
        em.remove(moim);
    }
}
