package com.peoplein.moiming.domain;


import com.peoplein.moiming.domain.enums.NotificationDomain;
import com.peoplein.moiming.domain.enums.NotificationDomainCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

// 알림
@Entity
@Getter
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "notification_id")
    private Long id;
    private Long senderId;    // 알림을 보낸 유저의 ID
    private boolean isRead;
    private String notiTitle;
    private String notiBody;
    private Long domainId;

    @Enumerated(value = EnumType.STRING)
    private NotificationDomain notiDomain;
    @Enumerated(value = EnumType.STRING)
    private NotificationDomainCategory notiCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static Notification createNotification(Long senderId, String notiTitle, String notiBody, Long domainId, NotificationDomain notiDomain, NotificationDomainCategory notiCategory, Member member) {

        Notification notification = new Notification(senderId,  notiTitle, notiBody, domainId, notiDomain, notiCategory, member);

        return notification;
    }

    private Notification(Long senderId, String notiTitle, String notiBody, Long domainId, NotificationDomain notiDomain, NotificationDomainCategory notiCategory, Member member) {

        // NULL 조건 추가 검증 필요
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), senderId, notiDomain, notiCategory, member);

        this.senderId = senderId;
        this.notiTitle = notiTitle;
        this.notiBody = notiBody;
        this.domainId = domainId;
        this.notiDomain = notiDomain;
        this.notiCategory = notiCategory;

        /*
         초기화
         */
        this.isRead = false;

        /*
         연관관계 매핑
         */
        this.member = member;
    }
}
