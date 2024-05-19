package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.peoplein.moiming.domain.enums.MemberGender;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class MemberInfoReqDto {

    @ApiModel(value = "Member Info API - 요청 - 회원 개인정보 등록")
    @Getter
    @Setter
    @AllArgsConstructor
    public static class MemberInfoCreateReqDto{

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate memberBirth;

        @NotNull
        private MemberGender memberGender;

    }
}
