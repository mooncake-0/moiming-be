package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.domain.Notification;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NotificationRespDto {


    @ApiModel(value = "Member Notification API - 응답 - 알림 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberNotificationRespDto{

        private Long notificationId;
        private boolean hasRead;
        private String topCategory;
        private String title;
        private String body;
        private Long topCategoryId;
        private Long subCategoryId; // SUB CATEGORY 는 게시글까지임
        private String createdAt;

        public MemberNotificationRespDto(Notification notification) {
            this.notificationId = notification.getId();
            this.hasRead = notification.isHasRead();
            this.topCategory = notification.getTopCategory().getValue();
            this.title = notification.getTitle();
            this.body = notification.getBody();
            this.topCategoryId = notification.getTopCategoryId();
            this.subCategoryId = notification.getSubCategoryId();
            this.createdAt = notification.getCreatedAt() + "";
        }

        // TODO :: Notification 의 소속된 Moim 에 대한 정보 전달 필요 예정
    }
}
