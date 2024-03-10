package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.ReportTarget;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

public class ReportReqDto {

    @ApiModel(value = "Report API - 요청 - 대상 신고")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportCreateReqDto {

        @NotNull
        private ReportTarget target;
        @NotNull
        private Long targetId;
        @NotNull
        private Integer reasonIndex;
        @NotNull
        private Boolean hasFiles;
    }
}
