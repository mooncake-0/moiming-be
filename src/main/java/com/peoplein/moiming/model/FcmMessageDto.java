package com.peoplein.moiming.model;


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
    }

    @Builder
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }
}
