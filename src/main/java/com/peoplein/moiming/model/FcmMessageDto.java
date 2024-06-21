package com.peoplein.moiming.model;


import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/*
 FCM Message Json 변환 Template
 */
@Builder
@Getter
public class FcmMessageDto {

    private Message message;

    @Builder
    @Getter
    public static class Message {
        private Notification notification;
        private String token;
        private Data data;
    }

    @Builder
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

    @Builder
    @Getter
    public static class Data {
        private String topCategory;
        private String subCategory;
        private String deepLink;
    }
}

/*
{
    "message":{
        "token" : "~fcmToken~",
        "notification": {
            "title":"-",
            "body":"~~가 모임에 가입하였습니다",
            "image": ""
        },
        "data":{
            "topCategory": "moim",
            "subCategory": "post_create"
            "deepLink":"moiming://topCategory/17/subCategory/28
        }
}
 */