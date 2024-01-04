//package com.peoplein.moiming.temp;
//
//import com.peoplein.moiming.repository.MemberSessionCategoryLinkerRepository;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//import static com.peoplein.moiming.domain.session.QMemberSessionLinker.memberSessionLinker;
//import static com.peoplein.moiming.domain.session.QMemberSessionCategoryLinker.*;
//
//@Repository
//@RequiredArgsConstructor
//public class MemberSessionCategoryLinkerJpaRepository implements MemberSessionCategoryLinkerRepository {
//
//    private final EntityManager em;
//    private final JPAQueryFactory queryFactory;
//
//
//    @Override
//    public void removeAll(List<Long> memberSessionLinkerIds) {
//        queryFactory.delete(memberSessionCategoryLinker)
//                .where(memberSessionCategoryLinker.memberSessionLinker.id.in(memberSessionLinkerIds)).execute();
//    }
//}
