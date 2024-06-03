package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.repository.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.member.QMember.*;
import static com.peoplein.moiming.domain.member.QMemberInfo.*;


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
    public void saveMemberInfo(MemberInfo memberInfo){
        em.persist(memberInfo);
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return Optional.ofNullable(queryFactory.selectFrom(member)
                .where(member.id.eq(memberId))
                .fetchOne()
        );
    }


    @Override
    public Optional<Member> findByMemberPhone(String memberPhone) {
        return Optional.ofNullable(
                queryFactory.selectFrom(member)
                        .where(member.memberPhone.eq(memberPhone))
                        .fetchOne()
        );
    }


    @Override
    public Optional<Member> findWithMemberInfoById(Long memberId) {
        return Optional.ofNullable(queryFactory.selectFrom(member)
                .leftJoin(member.memberInfo, memberInfo).fetchJoin()
                .where(member.id.eq(memberId))
                .fetchOne()
        );
    }


    @Override
    public Optional<Member> findByNickname(String nickname) {
        return Optional.ofNullable(queryFactory.selectFrom(member)
                .where(member.nickname.eq(nickname))
                .fetchOne()
        );
    }

//    v1.0 - DEPRECATED / SMS Verification 에선 더이상 MemberInfo 조회가 필요 없다
//    @Override
//    public Optional<Member> findWithMemberInfoByPhoneNumber(String memberPhoneNumber) {
//        return Optional.ofNullable(
//                queryFactory.selectFrom(member)
//                        .join(member.memberInfo, memberInfo).fetchJoin()
//                        .where(member.memberInfo.memberPhone.eq(memberPhoneNumber))
//                        .fetchOne()
//        );
//    }

//    v1.0 - DEPRECATED / 더이상 유니크 칼럼에 대한 확인을 진행하지 않는다
//    @Override
//    public List<Member> findMembersByEmailOrPhone(String memberEmail, String memberPhone) {
//        return queryFactory.selectFrom(member)
//                .join(member.memberInfo, memberInfo).fetchJoin()
//                .where(member.memberEmail.eq(memberEmail)
//                        .or(member.memberInfo.memberPhone.eq(memberPhone)))
//                .fetch();
//    }

}