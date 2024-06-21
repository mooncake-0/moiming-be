package com.peoplein.moiming.model.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

public class MemberSettingReqDto {

    @ApiModel(value = "Member Setting API - 요청 - 회원 설정 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberSettingUpdateReqDto{
        @NotNull
        private Boolean allowNoticeNotify;
        @NotNull
        private Boolean allowMoimNotify;
    }
}
