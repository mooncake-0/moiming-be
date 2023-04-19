package com.peoplein.moiming.domain;


import com.google.cloud.storage.Acl;
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
    private String notiInfo;
    private Long domainId;
    private String notiDomain;
    private String notiCategory; // 각 도메인별 Notification Category 의 종류

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static Notification createNotification(Long senderId, boolean isRead, String notiTitle, String notiInfo, Long domainId, String notiDomain, String notiCategory, Member member) {

        Notification notification = new Notification(senderId, isRead, notiTitle, notiInfo, domainId, notiDomain, notiCategory, member);

        return notification;
    }

    private Notification(Long senderId, boolean isRead, String notiTitle, String notiInfo, Long domainId, String notiDomain, String notiCategory, Member member) {

        // NULL 조건 추가 검증 필요
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), senderId);

        this.senderId = senderId;
        this.notiTitle = notiTitle;
        this.notiInfo = notiInfo;
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
