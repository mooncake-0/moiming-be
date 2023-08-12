package com.peoplein.moiming.repository.jpa.query;

import com.peoplein.moiming.model.query.QueryDuplicateColumnMemberDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.peoplein.moiming.domain.QMember.member;
import static com.peoplein.moiming.domain.QMemberInfo.memberInfo;

@Repository
@RequiredArgsConstructor
public class MemberJpaQueryRepository {

    private final JPAQueryFactory queryFactory;

    /*
     Querying Dtos
     */

    public List<QueryDuplicateColumnMemberDto> findDuplicateMemberByEmailOrPhone(String memberEmail, String memberPhone) {

        /*
         Query: select m.uid, mi.member_email from member m
                    join member_info mi
                    on m.member_info_id = mi.member_info_id
                    where m.uid = {uid} or mi.member_email = {memberEmail}
         */

        return queryFactory.select(Projections.constructor(QueryDuplicateColumnMemberDto.class, member.memberEmail, memberInfo.memberPhone))
                .from(member)
                .join(memberInfo)
                .on(member.memberInfo.id.eq(memberInfo.id))
                .where(member.memberEmail.eq(memberEmail).or(memberInfo.memberPhone.eq(memberPhone)))
                .fetch()
                ;
    }
}