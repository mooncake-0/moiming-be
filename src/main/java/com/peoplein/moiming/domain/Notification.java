package com.peoplein.moiming.domain;


import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.enums.NotificationType;
import com.peoplein.moiming.domain.member.Member;
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

    private Long receiverId;   // 연관관계를 거는게 오히려 저장할 때 불필요한 쿼리 발생 가능성, 인덱스 두는게 좋을 듯 (Member 단에서 많이 사용할듯)

    private boolean hasRead;

    private String title;

    private String body;

    private Long topCategoryId;

    private Long subCategoryId; // SUB CATEGORY 는 게시글까지임

    // 대분류
    @Enumerated(value = EnumType.STRING)
    private NotificationTopCategory topCategory;

    // 중분류
    @Enumerated(value = EnumType.STRING)
    private NotificationSubCategory subCategory;

    // 푸쉬 타입
    @Enumerated(value = EnumType.STRING)
    private NotificationType type;


    public Notification createNotification(NotificationTopCategory topCategory, NotificationSubCategory subCategory, NotificationType type
            , Long senderId, Long receiverId, String title, String body, Long topCategoryId, Long subCategoryId) {

        return new Notification(topCategory, subCategory, type, senderId, receiverId, title, body, topCategoryId, subCategoryId);

    }


    private Notification(NotificationTopCategory topCategory, NotificationSubCategory subCategory, NotificationType type
            , Long senderId, Long receiverId, String title, String body, Long topCategoryId, Long subCategoryId) {

        this.topCategory = topCategory;
        this.subCategory = subCategory;
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.title = title;
        this.body = body;
        this.topCategoryId = topCategoryId;
        this.subCategoryId = subCategoryId;
        this.hasRead = false;
    }
}
