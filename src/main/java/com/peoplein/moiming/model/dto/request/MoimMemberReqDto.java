package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class MoimMemberReqDto {

    @ApiModel(value = "Moim Member API - 요청 - 모임 가입하기")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimMemberJoinReqDto {
        @NotNull
        private Long moimId;
    }



    @ApiModel(value = "Moim Member API - 요청 - 모임 자의로 나가기")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimMemberLeaveReqDto {
        @NotNull
        private Long moimId;
    }


    @ApiModel(value = "Moim Member API - 요청 - 모임원 강퇴하기")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimMemberExpelReqDto {
        @NotNull
        private Long moimId;
        @NotNull
        private Long expelMemberId;
        @NotNull
        private String inactiveReason;
    }


    @ApiModel(value = "Moim Member API - 요청 - 운영진 임명하기")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimMemberGrantReqDto {

        @NotNull
        private Long moimId;

        @NotNull
        private Long grantMemberId;

        @NotNull
        @Pattern(regexp = "MANAGER")
        private MoimMemberRoleType roleType;

    }


}