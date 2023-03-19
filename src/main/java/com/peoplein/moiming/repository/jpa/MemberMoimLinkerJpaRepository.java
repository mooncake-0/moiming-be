package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.QMemberMoimLinker;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.QMemberMoimLinker.*;
import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMoim.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;

@Repository
@Transactional
@RequiredArgsConstructor
public class MemberMoimLinkerJpaRepository implements MemberMoimLinkerRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(MemberMoimLinker memberMoimLinker) {
        em.persist(memberMoimLinker);
        return memberMoimLinker.getId();
    }

    @Override
    public List<MemberMoimLinker> findByMemberId(Long memberId) {

        return queryFactory.selectFrom(memberMoimLinker)
                .where(memberMoimLinker.member.id.eq(memberId))
                .fetch();
    }

    @Override
    public List<MemberMoimLinker> findWithMoimByMemberId(Long memberId) {
        return queryFactory.selectFrom(memberMoimLinker)
                .join(memberMoimLinker.moim, moim).fetchJoin()
                .where(memberMoimLinker.member.id.eq(memberId))
                .fetch();
    }

    @Override
    public MemberMoimLinker findByMemberAndMoimId(Long memberId, Long moimId) {
        return queryFactory.selectFrom(memberMoimLinker)
                .where(memberMoimLinker.member.id.eq(memberId),
                        memberMoimLinker.moim.id.eq(moimId))
                .fetchOne();
    }

    @Override
    public Optional<MemberMoimLinker> findOptionalByMemberAndMoimId(Long memberId, Long moimId) {
        return Optional.ofNullable(queryFactory.selectFrom(memberMoimLinker)
                .where(memberMoimLinker.member.id.eq(memberId),
                        memberMoimLinker.moim.id.eq(moimId))
                .fetchOne());
    }

    @Override
    public MemberMoimLinker findWithMoimByMemberAndMoimId(Long memberId, Long moimId) {
        return queryFactory.selectFrom(memberMoimLinker)
                .join(memberMoimLinker.moim, moim).fetchJoin()
                .where(memberMoimLinker.member.id.eq(memberId),
                        memberMoimLinker.moim.id.eq(moimId))
                .fetchOne();
    }

    @Override
    public MemberMoimLinker findWithMemberInfoByMemberAndMoimId(Long memberId, Long moimId) { // Join 없음
        return queryFactory.selectFrom(memberMoimLinker)
                .join(memberMoimLinker.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(memberMoimLinker.member.id.eq(memberId),
                        memberMoimLinker.moim.id.eq(moimId))
                .fetchOne();
    }


    @Override
    public MemberMoimLinker findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId) {

       /*
         Query : select * from member_moim_linker mml
                    join member m on mml.member_id = m.member_id
                    join moim mo on mo.moim_id = mml.moim_id
                    where mml.member_id = {memberId}
                        and mml.moim_id = {moimId}
        */

        return queryFactory.selectFrom(memberMoimLinker)
                .join(memberMoimLinker.moim, moim).fetchJoin() // MEMO :: MemberMoimLinker 내부 Moim 정보를 주입시켜주기 위함, 없으면 moim select 절 나감
                .join(memberMoimLinker.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(memberMoimLinker.member.id.eq(memberId)
                        , memberMoimLinker.moim.id.eq(moimId))
                .fetchOne()
                ;
    }

    @Override
    public List<MemberMoimLinker> findWithMemberInfoAndMoimByMoimId(Long moimId) {

   /*
     Query : select * from member_moim_linker mml
                join member m on mml.member_id = m.member_id
                join moim mo on mo.moim_id = mml.moim_id
                where mml.moim_id = {moimId}
    */

        return queryFactory.selectFrom(memberMoimLinker)
                .join(memberMoimLinker.moim, moim).fetchJoin() // MEMO :: MemberMoimLinker 내부 Moim 정보를 주입시켜주기 위함, 없으면 moim select 절 나감
                .join(memberMoimLinker.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(memberMoimLinker.moim.id.eq(moimId))
                .fetch()
                ;
    }

    @Override
    public List<MemberMoimLinker> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds) {

        return queryFactory.selectFrom(memberMoimLinker)
                .join(memberMoimLinker.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(
                        memberMoimLinker.moim.id.eq(moimId),
                        memberMoimLinker.member.id.in(memberIds)
                )
                .fetch();
    }

    @Override
    public Optional<MemberMoimLinker> findOptionalWithMoimByMemberAndMoimId(Long memberId, Long moimId) {
        MemberMoimLinker memberMoimLinker = queryFactory.selectFrom(QMemberMoimLinker.memberMoimLinker)
                .join(QMemberMoimLinker.memberMoimLinker.moim, moim).fetchJoin()
                .where(QMemberMoimLinker.memberMoimLinker.member.id.eq(memberId),
                        QMemberMoimLinker.memberMoimLinker.moim.id.eq(moimId))
                .fetchOne();
        return Optional.ofNullable(memberMoimLinker);
    }

    @Override
    public void remove(MemberMoimLinker memberMoimLinker) {
        em.remove(memberMoimLinker);
    }

}
