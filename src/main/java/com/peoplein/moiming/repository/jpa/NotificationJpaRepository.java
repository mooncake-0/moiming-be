package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.repository.NotificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class NotificationJpaRepository implements NotificationRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public NotificationJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
    }


    @Override
    public Long save(Notification notification) {
        em.persist(notification);
        return notification.getId();
    }
}
