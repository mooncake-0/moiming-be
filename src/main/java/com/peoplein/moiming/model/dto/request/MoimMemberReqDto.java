package com.peoplein.moiming.model.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MoimMemberReqDto {

    @ApiModel(value = "Moim Member API - 요청 - 모임 자의로 나가기")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimMemberLeaveReqDto {
        private Long moimId;
    }


    @ApiModel(value = "Moim Member API - 요청 - 모임원 강퇴하기")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimMemberExpelReqDto {
        private Long moimId;
        private Long expelMemberId;
        private String inactiveReason;
    }


}