package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.NotificationDomain;
import com.peoplein.moiming.domain.enums.NotificationDomainCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDto {

    private Long notificationId;
    private Long senderId;    // 알림을 보낸 유저의 ID
    private String notiTitle;
    private String notiBody;
    private Long domainId;
    private NotificationDomain notiDomain;
    private NotificationDomainCategory notiCategory;

    public NotificationDto(Long notificationId, Long senderId, String notiTitle, String notiBody, Long domainId, NotificationDomain notiDomain, NotificationDomainCategory notiCategory) {
        this.notificationId = notificationId;
        this.senderId = senderId;
        this.notiTitle = notiTitle;
        this.notiBody = notiBody;
        this.domainId = domainId;
        this.notiDomain = notiDomain;
        this.notiCategory = notiCategory;
    }

    public void setNotiDomain(NotificationDomain notiDomain) {
        this.notiDomain = notiDomain;
    }

    public void setNotiCategory(NotificationDomainCategory notiCategory) {
        this.notiCategory = notiCategory;
    }
}
