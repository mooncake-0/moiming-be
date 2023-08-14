package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.repository.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;
import static com.peoplein.moiming.domain.QMemberRoleLinker.*;
import static com.peoplein.moiming.domain.fixed.QRole.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository implements MemberRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(Member member) {
        em.persist(member);
    }

    @Override
    public Member findMemberById(Long memberId) {

        /*
        Query : select * from member m
                where m.member_id = {id};
         */

        return queryFactory.selectFrom(member)
                .where(member.id.eq(memberId))
                .fetchOne();
    }

    @Override
    public Member findMemberAndMemberInfoById(Long memberId) {


        /*
         JPQL Query : select m from Member m
                        join fetch m.memberInfo mi where m.id = :id;

         */

        return queryFactory.selectFrom(member)
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(member.id.eq(memberId))
                .fetchOne();

    }

    @Override
    public Optional<Member> findMemberByEmail(String memberEmail) {

        return Optional.ofNullable(queryFactory.selectFrom(member)
                .where(member.memberEmail.eq(memberEmail))
                .fetchOne());
    }


    @Override
    public Member findMemberWithRolesByEmail(String memberEmail) {

        /*
         JPQL : select distinct m from Member m
                    join fetch m.roles mri
                    join fetch mri.role r
                    where m.memberEmail = {memberEmail}
         */

        return queryFactory.selectFrom(member).distinct()
                .join(member.roles, memberRoleLinker).fetchJoin()
                .join(memberRoleLinker.role, role).fetchJoin()
                .where(member.memberEmail.eq(memberEmail))
                .fetchOne();
    }

    @Override
    public Member findMemberAndMemberInfoWithRolesById(Long id) {
        /*
         JPQL : select distinct m from Member m
                    join fetch m.memberInfo mi
                    join fetch m.roles mri
                    join fetch mri.role r
                    where m.id = :{id}
         */

        return queryFactory.selectFrom(member).distinct()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .join(member.roles, memberRoleLinker).fetchJoin()
                .join(memberRoleLinker.role, role).fetchJoin()
                .where(member.id.eq(id))
                .fetchOne();
    }

    @Override
    public List<Member> findMembersByIds(List<Long> memberIds) {
        return queryFactory.selectFrom(member)
                .where(member.id.in(memberIds))
                .fetch();
    }

    @Override
    public Optional<Member> findOptionalByPhoneNumber(String memberPhoneNumber) {
        return Optional.ofNullable(
                queryFactory.selectFrom(member)
                        .join(member.memberInfo, memberInfo).fetchJoin()
                        .where(member.memberInfo.memberPhone.eq(memberPhoneNumber))
                        .fetchOne()
        );
    }


    @Override
    public List<Member> findByEmailOrPhone(String memberEmail, String memberPhone) {
        return queryFactory.selectFrom(member)
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(member.memberEmail.eq(memberEmail).or(member.memberInfo.memberPhone.eq(memberPhone)))
                .fetch();
    }

}