package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

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
        log.error("{}, {}", "존재하지 않는 정렬 기준 (Order By) 전환 시도, [" + value + "], C999", COMMON_INVALID_SITUATION.getErrMsg());
        throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
    }

}
