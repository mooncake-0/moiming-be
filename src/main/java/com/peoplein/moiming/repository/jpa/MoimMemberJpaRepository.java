package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.peoplein.moiming.domain.QPostComment.postComment;
import static com.peoplein.moiming.domain.moim.QMoim.*;
import static com.peoplein.moiming.domain.moim.QMoimMember.*;
import static com.peoplein.moiming.domain.member.QMember.*;
import static com.peoplein.moiming.domain.member.QMemberInfo.*;
import static com.peoplein.moiming.domain.QMoimCategoryLinker.*;
import static com.peoplein.moiming.domain.fixed.QCategory.*;
import static com.peoplein.moiming.domain.moim.QMoimJoinRule.*;

@Repository
@Transactional
@RequiredArgsConstructor
public class MoimMemberJpaRepository implements MoimMemberRepository {

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
    public void save(MoimMember moimMember) {
        checkIllegalQueryParams(moimMember);
        em.persist(moimMember);
    }



    @Override
    public List<MoimMember> findWithMoimAndCategoryByMemberId(Long memberId) {
        checkIllegalQueryParams(memberId);
        return queryFactory.selectFrom(moimMember).distinct() // MoimCategoryLinker 의 Collection 조회로 인한 중복 데이터 제거 필요
                .join(moimMember.moim, moim).fetchJoin()
                .join(moim.moimCategoryLinkers, moimCategoryLinker).fetchJoin()
                .join(moimCategoryLinker.category, category).fetchJoin()
                .where(moimMember.member.id.eq(memberId))
                .fetch();
    }


    @Override
    public Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId) {
        checkIllegalQueryParams(memberId, moimId);
        return Optional.ofNullable(queryFactory.selectFrom(moimMember)
                .where(moimMember.member.id.eq(memberId),
                        moimMember.moim.id.eq(moimId))
                .fetchOne());
    }


    @Override
    public MoimMember findWithMemberInfoByMemberAndMoimId(Long memberId, Long moimId) { // Join 없음

        return queryFactory.selectFrom(moimMember)
                .join(moimMember.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimMember.member.id.eq(memberId),
                        moimMember.moim.id.eq(moimId))
                .fetchOne();
    }


    @Override
    public MoimMember findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId) {

       /*
         Query : select * from member_moim_linker mml
                    join member m on mml.member_id = m.member_id
                    join moim mo on mo.moim_id = mml.moim_id
                    where mml.member_id = {memberId}
                        and mml.moim_id = {moimId}
        */

        return queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin() // MEMO :: MoimMember 내부 Moim 정보를 주입시켜주기 위함, 없으면 moim select 절 나감
                .join(moimMember.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimMember.member.id.eq(memberId)
                        , moimMember.moim.id.eq(moimId))
                .fetchOne()
                ;
    }

    @Override
    public List<MoimMember> findWithMemberInfoAndMoimByMoimId(Long moimId) {

   /*
     Query : select * from member_moim_linker mml
                join member m on mml.member_id = m.member_id
                join moim mo on mo.moim_id = mml.moim_id
                where mml.moim_id = {moimId}
    */

        return queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin() // MEMO :: MoimMember 내부 Moim 정보를 주입시켜주기 위함, 없으면 moim select 절 나감
                .join(moimMember.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimMember.moim.id.eq(moimId))
                .fetch()
                ;
    }



    @Override
    public Optional<MoimMember> findOptionalWithMoimByMemberAndMoimId(Long memberId, Long moimId) {
        return Optional.ofNullable(queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin()
                .where(moimMember.member.id.eq(memberId),
                        moimMember.moim.id.eq(moimId))
                .fetchOne());
    }

    @Override
    public void remove(MoimMember moimMember) {
        em.remove(moimMember);
    }

    // 각 모임 정보를 모두 같이 불러온다
    // In USE
    @Override
    public List<MoimMember> findWithMoimByMemberId(Long memberId) {
        checkIllegalQueryParams(memberId);
        return queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin()
                .where(moimMember.member.id.eq(memberId))
                .fetch();

    }


    @Override
    public List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds) {

        return queryFactory.selectFrom(moimMember)
                .join(moimMember.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(
                        moimMember.moim.id.eq(moimId),
                        moimMember.member.id.in(memberIds)
                )
                .fetch();
    }


    // 내가 가지고 있는 모임을 가져온다는 건..
    /*
    select from moim_member mm
            join moim m on m.moim_id == mm.moim_id
            where mm.member_id = {my_id} // 이게 기본 조건
    // 내가 운영하는 모임이라면 다음이 추가된다
                and (mm.member_role_type == MAMAGER)

    // 이 때, lastMoimId 가 있다면, 그거에서 부터 시작한다
                and (m.created_at < last_moim_created_at
                       or m.created_at == last_moim_created_at and m.id < given_id)

            order by m.created_at desc, m.moim_id desc
            limit 20; // 20개 씩 가져온다
    ;
     */

    @Override
    public List<MoimMember> findMemberMoimsWithRuleAndCategoriesByConditionsPaged(Long memberId
            , boolean isActiveReq, boolean isManagerReq, Moim lastMoim, int limit) {

        BooleanBuilder dynamicQuery = new BooleanBuilder();

        if (isActiveReq) { // 아닐 경우 다 들고온다, 보통 isActive 만 찾을 것임
            dynamicQuery.and(moimMember.memberState.eq(MoimMemberState.ACTIVE));
        }

        if (isManagerReq) {
            dynamicQuery.and(moimMember.memberRoleType.eq(MoimMemberRoleType.MANAGER));
        }

        if (lastMoim != null) {
            dynamicQuery.and(
                    moimMember.moim.createdAt.before(lastMoim.getCreatedAt())
                            .or(
                                    moimMember.moim.createdAt.eq(lastMoim.getCreatedAt())
                                            .and(moimMember.moim.id.lt(lastMoim.getId()))
                            )
            );
        }

        // TODO :: moimCategoryLinkers 는 일대다 관계를 가져오려는 것. Fetch Join 이 가능한게 맞는가? 되는걸 보면 맞긴 함
        return queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin()
                .leftJoin(moim.moimJoinRule, moimJoinRule).fetchJoin()
                .join(moim.moimCategoryLinkers, moimCategoryLinker).fetchJoin()
                .where(moimMember.member.id.eq(memberId), dynamicQuery)// 기본 where 외로 and 가 붙는다
                .orderBy(moimMember.moim.createdAt.desc(), moimMember.moim.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public void removeAllByMoimId(Long moimId) {
        queryFactory.delete(moimMember).where(moimMember.moim.id.eq(moimId)).execute();
    }

}
