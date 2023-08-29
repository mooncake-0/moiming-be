package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.moim.QMoim.*;
import static com.peoplein.moiming.domain.moim.QMoimMember.*;
import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;

@Repository
@Transactional
@RequiredArgsConstructor
public class MoimMemberJpaRepository implements MoimMemberRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(MoimMember moimMember) {
        em.persist(moimMember);
    }

    @Override
    public List<MoimMember> findByMemberId(Long memberId) {

        return queryFactory.selectFrom(moimMember)
                .where(moimMember.member.id.eq(memberId))
                .fetch();
    }

    @Override
    public List<MoimMember> findWithMoimByMemberId(Long memberId) {
        return queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin()
                .where(moimMember.member.id.eq(memberId))
                .fetch();
    }

    @Override
    public Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId) {
        return Optional.ofNullable(queryFactory.selectFrom(moimMember)
                .where(moimMember.member.id.eq(memberId),
                        moimMember.moim.id.eq(moimId))
                .fetchOne());
    }


    @Override
    public MoimMember findWithMoimByMemberAndMoimId(Long memberId, Long moimId) {
        return queryFactory.selectFrom(moimMember)
                .join(moimMember.moim, moim).fetchJoin()
                .where(moimMember.member.id.eq(memberId),
                        moimMember.moim.id.eq(moimId))
                .fetchOne();
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

}
