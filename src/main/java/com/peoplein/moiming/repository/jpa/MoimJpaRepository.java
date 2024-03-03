package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.MoimRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.moim.QMoimMember.*;
import static com.peoplein.moiming.domain.moim.QMoimJoinRule.*;
import static com.peoplein.moiming.domain.moim.QMoim.*;
import static com.peoplein.moiming.domain.QMoimCategoryLinker.*;
import static com.peoplein.moiming.domain.fixed.QCategory.*;
import static com.peoplein.moiming.domain.member.QMember.*;

@Repository
@RequiredArgsConstructor
public class MoimJpaRepository implements MoimRepository {


    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    private void checkIllegalQueryParams(Object... objs) {
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
    public Optional<Moim> findWithMoimMemberAndMemberById(Long moimId) {

        checkIllegalQueryParams(moimId);

        return Optional.ofNullable(queryFactory.selectFrom(moim).distinct()
                .join(moim.moimMembers, moimMember).fetchJoin()
                .join(moimMember.member, member).fetchJoin()
                .where(moim.id.eq(moimId))
                .fetchOne());
    }


    @Override
    public Optional<Moim> findWithJoinRuleAndCategoriesById(Long moimId) {

        return Optional.ofNullable(
                queryFactory.selectFrom(moim).distinct()
                        .join(moim.moimCategoryLinkers, moimCategoryLinker).fetchJoin()
                        .join(moimCategoryLinker.category, category).fetchJoin()
                        .leftJoin(moim.moimJoinRule, moimJoinRule).fetchJoin()
                        .where(moim.id.eq(moimId))
                        .fetchOne()
        );
    }


    // Moim List 조회가 아니므로 distinct 도 필요 없음
    // MOIM 에 대한 MoimMember EAGER 조회
    @Override
    public Optional<Moim> findWithActiveMoimMembersById(Long moimId) {

        return Optional.ofNullable(queryFactory.selectFrom(moim)
                .from(moim.moimMembers, moimMember).fetchJoin()
                .where(moim.id.eq(moimId),
                        moimMember.memberState.eq(MoimMemberState.ACTIVE))
                .fetchOne());

    }


    @Override
    public void remove(Long moimId) {
        queryFactory.delete(moim).where(moim.id.eq(moimId)).execute();
    }

}
