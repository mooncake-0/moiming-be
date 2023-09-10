package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.moim.Moim;
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

import static com.peoplein.moiming.domain.moim.QMoimMember.*;
import static com.peoplein.moiming.domain.moim.QMoimJoinRule.*;
import static com.peoplein.moiming.domain.moim.QMoim.*;
import static com.peoplein.moiming.domain.QMoimCategoryLinker.*;

@Repository
@RequiredArgsConstructor
public class MoimJpaRepository implements MoimRepository {


    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(Moim moim) {

        em.persist(moim);
    }


    @Override
    public Optional<Moim> findById(Long moimId) {

        return Optional.ofNullable(queryFactory.selectFrom(moim)
                .where(moim.id.eq(moimId))
                .fetchOne());
    }

    @Override
    public Optional<Moim> findWithJoinRuleById(Long moimId) {
        /*
         Query : select m from Moim m
                    join fetch m.moimRules mr
                    where m.id = :moimId;
        */

        return Optional.ofNullable(queryFactory.selectFrom(moim)
                .join(moim.moimJoinRule, moimJoinRule).fetchJoin()
                .where(moim.id.eq(moimId))
                .fetchOne());
    }


    @Override
    public Optional<Moim> findWithMoimMembersById(Long moimId) {
        return Optional.ofNullable(queryFactory.selectFrom(moim).distinct()
                .join(moim.moimMembers, moimMember).fetchJoin()
                .where(moim.id.eq(moimId))
                .fetchOne());
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

    @Override
    public void remove(Moim moim) {
        em.remove(moim);
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
