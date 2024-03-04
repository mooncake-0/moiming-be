package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.repository.NotificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationJpaRepository implements NotificationRepository {

    private final EntityManager em;

    @Override
    public void save(Notification notification) {
        em.persist(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        String jpql = "SELECT n FROM Notification n WHERE n.id = :id";
        return Optional.ofNullable(em.createQuery(jpql, Notification.class)
                .setParameter("id", id)
                .getSingleResult());
    }

    @Override
    public List<Notification> findMemberNotificationByCondition(Long memberId, NotificationTopCategory topCategory, String moimType, Notification lastNotification, int limit) {

        String jpql =
                "SELECT n FROM Notification n " +
                        "JOIN Moim m ON n.topCategoryId = m.id " +
                        "WHERE n.receiverId = :memberId " +
                        "AND n.topCategory = :topCategory ";

        if (moimType.equals("manage")) {
            // n.topCategoryId 로 Join 을 해서 m.creatorId 가 나인 것을 가져와야 한다
            jpql += "AND m.creatorId = :memberId ";
        } else if (moimType.equals("join")) {
            // n.topCategoryId 로 Join 을 해서 m.creatorId 가 내가 아닌 것을 가져와야 한다
            jpql += "AND m.creatorId <> :memberId ";
        }

        if (lastNotification != null) {
            jpql += "AND ((n.createdAt < :lastNotificationCreatedAt) OR (n.createdAt = :lastNotificationCreatedAt AND n.id < :lastNotificationId))";
        }


        jpql += "ORDER BY n.createdAt DESC, n.id DESC";


        TypedQuery<Notification> query = em.createQuery(jpql, Notification.class)
                .setParameter("memberId", memberId)
                .setParameter("topCategory", topCategory);

        if (lastNotification != null) {
            query.setParameter("lastNotificationCreatedAt", lastNotification.getCreatedAt())
                    .setParameter("lastNotificationId", lastNotification.getId());
        }

        return query.setMaxResults(limit) // limit 절
                .getResultList();

    }

    @Override
    public void remove(Notification notification) {
        em.remove(notification);
    }

}
