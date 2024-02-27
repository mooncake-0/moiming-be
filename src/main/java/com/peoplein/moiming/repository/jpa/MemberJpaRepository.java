package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.member.Member;
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
    public Optional<Member> findById(Long memberId) {

        /*
        Query : select * from member m
                where m.member_id = {id};
         */

        return Optional.ofNullable(queryFactory.selectFrom(member)
                .where(member.id.eq(memberId))
                .fetchOne());
    }


    /*
     member info 활용을 위해 join 추가
     */
    @Override
    public Optional<Member> findByEmail(String memberEmail) {

        return Optional.ofNullable(queryFactory.selectFrom(member)
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(member.memberEmail.eq(memberEmail))
                .fetchOne());
    }


    @Override
    public Optional<Member> findByNickname(String nickname) {
        return Optional.ofNullable(queryFactory.selectFrom(member)
                .where(member.nickname.eq(nickname))
                .fetchOne()
        );
    }


    @Override
    public Optional<Member> findWithMemberInfoByPhoneNumber(String memberPhoneNumber) {
        return Optional.ofNullable(
                queryFactory.selectFrom(member)
                        .join(member.memberInfo, memberInfo).fetchJoin()
                        .where(member.memberInfo.memberPhone.eq(memberPhoneNumber))
                        .fetchOne()
        );
    }


    @Override
    public List<Member> findMembersByIds(List<Long> memberIds) {
        return queryFactory.selectFrom(member)
                .where(member.id.in(memberIds))
                .fetch();
    }


    @Override
    public List<Member> findMembersByEmailOrPhone(String memberEmail, String memberPhone) {
        return queryFactory.selectFrom(member)
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(member.memberEmail.eq(memberEmail)
                        .or(member.memberInfo.memberPhone.eq(memberPhone)))
                        .fetch();
    }

    @Override
    public void updateRefreshTokenById(Long id, String refreshToken) {
        long num = queryFactory.update(member)
                .set(member.refreshToken, refreshToken)
                .where(member.id.eq(id))
                .execute();

        if (num != 1) {
            throw new RuntimeException("일단 에러인데, 나중에 잡는거 처리할거임");
        }
    }

}