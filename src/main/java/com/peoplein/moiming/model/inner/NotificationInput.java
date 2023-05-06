package com.peoplein.moiming.model.inner;

import com.peoplein.moiming.domain.enums.NotificationDomain;
import com.peoplein.moiming.domain.enums.NotificationDomainCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 생성시 내부적으로 정보 전달을 위한 클래스
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationInput {

    private Long senderId;
    private Long domainId;
    private NotificationDomain notiDomain;
    private NotificationDomainCategory notiDomainCategory;

    public NotificationInput(Long senderId, Long domainId, NotificationDomain notiDomain, NotificationDomainCategory notiDomainCategory) {
        this.senderId = senderId;
        this.domainId = domainId;
        this.notiDomain = notiDomain;
        this.notiDomainCategory = notiDomainCategory;
    }
}
