package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.session.MemberSessionLinker;
import com.peoplein.moiming.repository.MemberSessionLinkerRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.domain.session.QMemberSessionLinker.*;
@Repository
@RequiredArgsConstructor
public class MemberSessionLinkerJpaRepository implements MemberSessionLinkerRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(MemberSessionLinker memberSessionLinker) {
        em.persist(memberSessionLinker);
        return memberSessionLinker.getId();
    }

    @Override
    public void removeAll(Long moimSessionId) {
        queryFactory.delete(memberSessionLinker).where(memberSessionLinker.moimSession.id.eq(moimSessionId)).execute();
    }
}
