package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;
import static com.peoplein.moiming.exception.ExceptionValue.COMMON_MAPPABLE_ENUM_VALUE;

@Slf4j
@Getter
public enum OrderBy {

    date;

    public static OrderBy findOrderBy(String value) {
        for (OrderBy ob : OrderBy.values()) {
            if (ob.toString().equals(value)) {
                return ob;
            }
        }
        // Order By 필터가 지원되지 않습니다
        log.error("{}, findOrderBy :: {}", "OrderBy", "[" + value + "] 에 해당하는 정렬이 지원되지 않습니다");
        throw new MoimingApiException(COMMON_MAPPABLE_ENUM_VALUE);
    }
}
