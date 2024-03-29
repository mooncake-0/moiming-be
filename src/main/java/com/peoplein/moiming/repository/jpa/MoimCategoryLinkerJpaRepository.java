package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.repository.MoimCategoryLinkerRepository;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.domain.QMoimCategoryLinker.*;
import static com.peoplein.moiming.domain.fixed.QCategory.*;

@Repository
@RequiredArgsConstructor
public class MoimCategoryLinkerJpaRepository implements MoimCategoryLinkerRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public void save(MoimCategoryLinker moimCategoryLinker) {
        em.persist(moimCategoryLinker);
    }

    @Override
    public List<MoimCategoryLinker> findWithCategoryByMoimIds(List<Long> moimIds) {

        return queryFactory.selectFrom(moimCategoryLinker)
                .join(moimCategoryLinker.category, category).fetchJoin()
                .where(moimCategoryLinker.moim.id.in(moimIds))
                .fetch();

    }

    @Override
    public void removeAllByMoimId(Long moimId) {
        JPADeleteClause clause = new JPADeleteClause(em, moimCategoryLinker);
        clause.where(moimCategoryLinker.moim.id.eq(moimId)).execute();
    }
}
