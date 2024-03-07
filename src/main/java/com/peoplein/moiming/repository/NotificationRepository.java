package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    void save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findMemberNotificationByCondition(Long memberId, NotificationTopCategory topCategory, String moimType, Notification lastNotification, int limit);

    void remove(Notification notification);
}
