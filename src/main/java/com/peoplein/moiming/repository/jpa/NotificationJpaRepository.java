package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.repository.NotificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class NotificationJpaRepository implements NotificationRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(Notification notification) {
        em.persist(notification);
    }
}
