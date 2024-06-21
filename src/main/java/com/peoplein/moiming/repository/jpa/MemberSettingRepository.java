package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.member.MemberSetting;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.member.QMemberSetting.*;

@Repository
@RequiredArgsConstructor
public class MemberSettingRepository {

    private EntityManager em;
    private final JPAQueryFactory queryFactory;


    public Optional<MemberSetting> findById(Long memberId) {
        return Optional.ofNullable(queryFactory.selectFrom(memberSetting)
                .where(memberSetting.id.eq(memberId))
                .fetchOne());
    }


    public List<MemberSetting> findByIds(List<Long> memberIds) {
        return queryFactory.selectFrom(memberSetting)
                .where(memberSetting.id.in(memberIds))
                .fetch();


    }
}
