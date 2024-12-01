package com.peoplein.moiming.service.util;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import com.peoplein.moiming.exception.MoimingApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class MemberPasswordManager {

    private final PasswordEncoder passwordEncoder;

    public MemberPasswordManager(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encodePwWhenCreate(String givenPw) {
        validatePw(givenPw);
        return passwordEncoder.encode(givenPw);
    }

    public String encodePwWhenUpdate(String givenPw, String preEncodedPw) {

        validatePw(givenPw);

        // 추가 검증
        if (passwordEncoder.matches(givenPw, preEncodedPw)) { // 현재 일치하는 PASSWORD
            throw new MoimingApiException(COMMON_INVALID_SITUATION, "현재 비밀번호와 일치해서 변경되지 않음"); // TODO:: 예외 Refactor
        }

        return passwordEncoder.encode(givenPw);
    }

    private void validatePw(String givenPw) {

        // 길이가 8자 이상~20자 이하인지와 아래 조건을 만족하는지?
        if (!StringUtils.hasText(givenPw) || givenPw.length() < 8 || givenPw.length() > 20) {
            throw new MoimingApiException(COMMON_CONSTRAINT_INVALID, "비밀번호는 8자 이상 20자 이하입니다");
        }

        String ENG_NUM_REG = "^(?=.*[A-Za-z])(?=.*\\d).+$"; // 영문 + 숫자
        String ENG_SPEC_REG = "^(?=.*[A-Za-z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$"; // 영문 + 특수문자

        boolean isAlphaNumeric = Pattern.matches(ENG_NUM_REG, givenPw); // 1번 검사
        boolean isAlphaSpecial = Pattern.matches(ENG_SPEC_REG, givenPw); // 2번 검사

        if (!isAlphaNumeric && !isAlphaSpecial) {
            throw new MoimingApiException(COMMON_CONSTRAINT_INVALID, "비밀번호의 조합 조건 (영문 / 숫자 / 특수문자) 을 부합하지 못했습니다");
        }
    }
}
