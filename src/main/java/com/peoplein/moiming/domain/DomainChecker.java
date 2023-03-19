package com.peoplein.moiming.domain;

import com.peoplein.moiming.exception.BadAuthParameterInputException;
import com.peoplein.moiming.security.exception.AuthErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DomainChecker {


    /*
     필수 입력값들이 포함된 String 들 Checker
     cause - 에러 발생시 msg 형성 지원
     isAuth - Auth Error 구분자
     */
    public static void checkRightString(String cause, boolean isAuth, String... strings) {
        AtomicInteger counter = new AtomicInteger(0);
        boolean hasWrong = Arrays.stream(strings).anyMatch(myString -> {
            if (!StringUtils.hasText(myString)) {
                return true;
            }
            counter.getAndIncrement();
            return false;
        });

        if (hasWrong) {
            if (isAuth)
                throw new BadAuthParameterInputException("공백으로 제출된 값이 있습니다 : " + cause + "- index [" + counter + "] ", AuthErrorEnum.AUTH_SIGNIN_INVALID_INPUT.getErrorCode());
            else throw new IllegalArgumentException("잘못된 입력이 발생하였습니다 : " + cause + "- index [" + counter + "] ");
        }
    }

    /*
     Parameter Objects 들의 Null 여부 Checker
     */
    public static void checkWrongObjectParams(String cause, Object... objs) {
        AtomicInteger counter = new AtomicInteger(0);
        boolean hasWrong = Arrays.stream(objs).anyMatch(obj -> {
            if (Objects.isNull(obj)) {
                return true;
            }
            counter.getAndIncrement();
            return false;
        });
        if (hasWrong) {
            throw new NullPointerException("잘못된 객체가 전달되었습니다 : " + cause + "- index [" + counter + "] ");
        }
    }

}
