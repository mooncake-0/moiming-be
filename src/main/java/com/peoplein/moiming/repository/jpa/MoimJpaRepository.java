package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.QMoim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.repository.MoimRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.QMoim.*;
import static com.peoplein.moiming.domain.QMoimCategoryLinker.*;
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

    @Override
    public List<Moim> findMoimBySearchCondition(List<String> keywordList, Area area, Category category) {
        JPAQuery<Moim> query = queryFactory.selectFrom(moim)
                .where(areaEq(area), keywordEq(keywordList));
        addJoinQuery(query, category);
        return query.fetch();
    }

    @Override
    public List<Moim> findAllMoim() {
        return queryFactory
                .selectFrom(moim)
                .fetch();
    }

    private void addJoinQuery(JPAQuery<Moim> query, Category category) {
        if (category == null)
            return;

        query.leftJoin(moimCategoryLinker)
                .on(moimCategoryLinker.moim.id.eq(moim.id).and(moimCategoryLinker.category.id.eq(category.getId())));
    }

    private Predicate keywordEq(List<String> keywordsList) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        keywordsList.forEach(keyword -> booleanBuilder.or(moim.moimName.like("%" + keyword + "%")));
        return booleanBuilder.getValue();
    }

    private BooleanExpression areaEq(Area area) {
        return area != null ? moim.moimArea.eq(area) : null;
    }
}
