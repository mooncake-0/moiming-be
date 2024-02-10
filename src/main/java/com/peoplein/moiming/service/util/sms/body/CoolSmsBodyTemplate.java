package com.peoplein.moiming.service.util.sms.body;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 Cool SMS API 변환 Template
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoolSmsBodyTemplate {

    private CoolSmsMessage message;

    public CoolSmsBodyTemplate(String to, String from, String text) {
        this.message = new CoolSmsMessage(to, from, text);
    }

    @Getter
    public static class CoolSmsMessage{
        private String to;
        private String from;
        private String text;
        private String type;

        public CoolSmsMessage(String to, String from, String text ) {
            this.to = to;
            this.from = from;
            this.text = text;
            this.type = "SMS";
        }
    }
}
