package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

@Slf4j
@Getter
public enum NotificationTopCategory {
    MOIM("모임"),
    DEFAULT("기타"),
    NOTICE("공지");

    private final String value;

    NotificationTopCategory(String value) {
        this.value = value;
    }

    public static NotificationTopCategory fromValue(String value) {
        for (NotificationTopCategory topCategory : NotificationTopCategory.values()) {
            if (topCategory.getValue().equals(value)) {
                return topCategory;
            }
        }
        log.error("{}, {}", "존재하지 않는 알림 1차 종류로 전환 시도, [" + value + "], C999", COMMON_INVALID_SITUATION.getErrMsg());
        throw new MoimingApiException(COMMON_INVALID_SITUATION);
    }
}
