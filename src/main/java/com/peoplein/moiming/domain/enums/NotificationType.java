package com.peoplein.moiming.domain.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    INFORM("정보성"),
    PROMOTION("광고성");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }
}
