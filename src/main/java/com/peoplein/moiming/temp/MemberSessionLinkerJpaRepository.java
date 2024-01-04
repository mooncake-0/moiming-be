//package com.peoplein.moiming.temp;
//
//import com.peoplein.moiming.temp.session.MemberSessionLinker;
//import com.peoplein.moiming.repository.MemberSessionLinkerRepository;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//
//import java.util.Optional;
//
//import static com.peoplein.moiming.domain.session.QMemberSessionLinker.*;
//import static com.peoplein.moiming.domain.session.QMoimSession.*;
//
//@Repository
//@RequiredArgsConstructor
//public class MemberSessionLinkerJpaRepository implements MemberSessionLinkerRepository {
//
//    private final EntityManager em;
//    private final JPAQueryFactory queryFactory;
//
//    @Override
//    public Long save(MemberSessionLinker memberSessionLinker) {
//        em.persist(memberSessionLinker);
//        return memberSessionLinker.getId();
//    }
//
//    @Override
//    public Optional<MemberSessionLinker> findOptionalByMemberAndSessionId(Long memberId, Long sessionId) {
//
//        return Optional.ofNullable(queryFactory.selectFrom(memberSessionLinker)
//                .join(memberSessionLinker.moimSession, moimSession).fetchJoin()
//                .where(memberSessionLinker.member.id.eq(memberId)
//                        , memberSessionLinker.moimSession.id.eq(sessionId))
//                .fetchOne());
//    }
//
//    @Override
//    public void removeAll(Long moimSessionId) {
//        queryFactory.delete(memberSessionLinker).where(memberSessionLinker.moimSession.id.eq(moimSessionId)).execute();
//    }
//}
