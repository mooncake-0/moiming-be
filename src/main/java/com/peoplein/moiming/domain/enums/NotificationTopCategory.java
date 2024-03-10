package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_REQUEST_PARAM;
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

        log.error("{}, fromValue :: {}", "NotificationTopCategory", "[" + value + "] 에 해당하는 객체를 찾을 수 없습니다");
        throw new MoimingApiException(ExceptionValue.COMMON_MAPPABLE_ENUM_VALUE);
    }



    public static NotificationTopCategory fromQueryParam(String value) {
        for (NotificationTopCategory topCategory : NotificationTopCategory.values()) {
            if (topCategory.getValue().equals(value)) {
                return topCategory;
            }
        }
        log.error("{}, fromQueryParam :: {}", "NotificationTopCategory", "[" + value + "] 에 해당하는 객체를 찾을 수 없습니다");
        throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM);
    }
}
