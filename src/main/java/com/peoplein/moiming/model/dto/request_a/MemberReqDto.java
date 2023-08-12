package com.peoplein.moiming.model.dto.request_b;

import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class MemberReqDto {


    @Getter
    @Setter
    @AllArgsConstructor
    public static class MemberSignInReqDto{

        @NotEmpty
        @Pattern(regexp = "^[a-zA-Z0-9]{1,30}@[a-z]{1,20}\\.[a-z]{1,10}$", message = "{1~30자리 영문 + 숫자}@{1~20자리 영문(소문자)}.{1~10자리 영문(소문자)}")
        private String memberEmail;

        // TODO :: ENCODING AOP 로 빼주면 좀 더 수월할듯 (VALIDATION 도 같이 수행)
        @NotEmpty
        @Size(min = 4, max = 20, message = "4자~20자 (조건 Prod 시 추가 예정)")
        private String password;

        /*
         TODO :: 부가적 조건들 추가 예정
         */
        @NotEmpty
        private String memberName;

        @NotEmpty
        private String memberPhone;

        @NotNull
        private MemberGender memberGender;

        @NotNull
        private LocalDate memberBirth;

        @NotNull
        private String fcmToken;

    }
}
