package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.session.SessionCategoryItem;
import com.peoplein.moiming.repository.SessionCategoryItemRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.domain.session.QSessionCategoryItem.*;
@Repository
@RequiredArgsConstructor
public class SessionCategoryItemJpaRepository implements SessionCategoryItemRepository {

    private EntityManager em;
    private JPAQueryFactory queryFactory;

    @Override
    public Long save(SessionCategoryItem sessionCategoryItem) {
        em.persist(sessionCategoryItem);
        return sessionCategoryItem.getId();
    }

    @Override
    public void removeAll(Long moimSessionId) {
        queryFactory.delete(sessionCategoryItem).where(sessionCategoryItem.moimSession.id.eq(moimSessionId)).execute();
    }
}
