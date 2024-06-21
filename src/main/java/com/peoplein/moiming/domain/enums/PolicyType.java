package com.peoplein.moiming.domain.enums;


import lombok.Getter;

@Getter
public enum PolicyType {

    SERVICE(false), // 서비스 이용 약관
    PRIVACY(false), // 개인정보 수집
    AGE(false), // 만 14세 이상 정보
    MARKETING_AGREE(true), // 마케팅 목적의 개인정보 수집 및 이용 동의
    MARKETING_PUSH(true), // 마케팅 정보 수신 여부 _ 앱 푸시
    MARKETING_SMS(true); // 마케팅 정보 수신 여부 _ 문자

    private final boolean optional;

    PolicyType(boolean optional) {
        this.optional = optional;
    }

}
