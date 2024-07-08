package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Getter
public enum NotificationTopCategory {

    MOIM("모임활동"),
    NOTICE("공지사항"),
    DEFAULT("기타");

    private final String name;

    NotificationTopCategory(String name) {
        this.name = name;
    }


    /*
     ENUM Value 자체로 파싱
     */
    public static NotificationTopCategory fromValue(String value) {
        for (NotificationTopCategory topCategory : NotificationTopCategory.values()) {
            if (topCategory.toString().equalsIgnoreCase(value)) {
                return topCategory;
            }
        }

        log.info("{}, fromValue :: {}", "NotificationTopCategory", "[" + value + "] 에 해당하는 객체를 찾을 수 없습니다");
        throw new MoimingApiException(COMMON_MAPPABLE_ENUM_VALUE);
    }

}
