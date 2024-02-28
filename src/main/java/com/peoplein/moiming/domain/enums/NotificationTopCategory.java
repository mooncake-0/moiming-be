package com.peoplein.moiming.domain.enums;

import lombok.Getter;
import lombok.val;

@Getter
public enum NotificationTopCategory {
    MOIM("모임"),
    DEFAULT("기타"),
    NOTICE("공지");

    private final String value;

    NotificationTopCategory(String value) {
        this.value = value;

    }
}
