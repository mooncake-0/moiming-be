package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.MoimRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.QueryParameterException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    private void checkIllegalQueryParams(Object ... objs) {
        for (Object obj : objs) {
            if (Objects.isNull(obj)) {
                throw new InvalidQueryParameterException("쿼리 파라미터는 NULL 일 수 없습니다");
            }
        }
    }

    @Override
    public void save(Moim moim) {
        checkIllegalQueryParams(moim);
        em.persist(moim);
    }


    @Override
    public Optional<Moim> findById(Long moimId) {
        checkIllegalQueryParams(moimId);
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
        checkIllegalQueryParams(moimId);
        return Optional.ofNullable(queryFactory.selectFrom(moim)
                .leftJoin(moim.moimJoinRule, moimJoinRule).fetchJoin()
                .where(moim.id.eq(moimId))
                .fetchOne());
    }


    @Override
    public Optional<Moim> findWithMoimMembersById(Long moimId) {

        checkIllegalQueryParams(moimId);

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
    public Optional<Moim> findWithJoinRuleAndCategoryById(Long moimId) {

        return Optional.ofNullable(
                queryFactory.selectFrom(moim).distinct()
                        .join(moim.moimCategoryLinkers, moimCategoryLinker).fetchJoin()
                        .leftJoin(moim.moimJoinRule, moimJoinRule).fetchJoin()
                        .where(moim.id.eq(moimId))
                        .fetchOne()
        );
    }


    @Override
    public void remove(Long moimId) {
        queryFactory.delete(moim).where(moim.id.eq(moimId)).execute();
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
