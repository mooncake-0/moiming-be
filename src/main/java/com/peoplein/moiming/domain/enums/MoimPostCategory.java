package com.peoplein.moiming.domain.enums;


import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_REQUEST_PARAM;
import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

@Slf4j
@Getter
@AllArgsConstructor
public enum MoimPostCategory {

    NOTICE("공지"), // 공지 게시물은 운영진에게만 제공
    GREETING("가입인사"),
    REVIEW("후기"),
    EXTRA("자유글");

    private final String value;

    public static MoimPostCategory fromValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (MoimPostCategory cName : MoimPostCategory.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        log.error("{}, {}", "존재하지 않는 게시물 종류로 전환 시도, [" + value + "], C999", COMMON_INVALID_SITUATION.getErrMsg());
        throw new MoimingApiException(COMMON_INVALID_SITUATION);
    }


    public static MoimPostCategory fromQueryParam(String value) {
        for (MoimPostCategory cName : MoimPostCategory.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        log.error("{}, fromQueryParam :: {}", "MoimPostCategory",  "존재하지 않는 게시물 종류로 게시물 조회 시도, [" + value + "]");
        throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM);
    }

}
