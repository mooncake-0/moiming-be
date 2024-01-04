//package com.peoplein.moiming.temp;
//
//import com.peoplein.moiming.domain.fixed.SessionCategory;
//import com.peoplein.moiming.repository.SessionCategoryRepository;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//import static com.peoplein.moiming.domain.fixed.QSessionCategory.*;
//
//@Repository
//@RequiredArgsConstructor
//public class SessionCategoryJpaRepository implements SessionCategoryRepository {
//
//    private final EntityManager em;
//
//    private final JPAQueryFactory queryFactory;
//
//
//    @Override
//    public List<SessionCategory> findAllSessionCategories() {
//        return queryFactory.selectFrom(sessionCategory).fetch();
//    }
//}
