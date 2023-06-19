package com.peoplein.moiming.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/*
 NAVER SMS API 변환 Template
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsMessageDto {

    private String type; // SMS
    private String contentType; // COMM : 일반메시지
    private String countryCode; // 대한민국 82
    private String from; // 송신 번호
    private String content; // 문자 내용
    private List<Messages> messages;

    public static SmsMessageDto createSmsMessageDto(String content, String... phoneNums) {
        return new SmsMessageDto(content, phoneNums);
    }

    private SmsMessageDto(String content, String... phoneNums) {
        this.type = "SMS";
        this.content = "COMM";
        this.countryCode = "82";
        this.from = "01030967210";
        this.content = content;
        this.messages = createMessageList(phoneNums);
    }

    private List<Messages> createMessageList(String[] phoneNums) {

        List<Messages> messages = new ArrayList<>();
        for (String phoneNum : phoneNums) {
            messages.add(new Messages(phoneNum));
        }
        return messages;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Messages {
        private String to; // 보내려는 전화번호

        public Messages(String to) {
            this.to = to;
        }
    }
}
