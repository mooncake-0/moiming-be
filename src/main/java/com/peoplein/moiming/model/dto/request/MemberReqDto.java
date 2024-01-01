package com.peoplein.moiming.model.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

public class MemberReqDto {

    @ApiModel(value = "Member API - 요청 - 비밀번호 확인")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberConfirmPwReqDto{

        @NotEmpty
        private String password;

    }


    @ApiModel(value = "Member API - 요청 - 닉네임 변경")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberChangeNicknameReqDto{

        @NotEmpty
        private String nickname;

    }

    @ApiModel(value = "Member API - 요청 - 비밀번호 변경")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberChangePwReqDto{

        @NotEmpty
        private String prePw;

        @NotEmpty
        private String postPw;

    }
}

