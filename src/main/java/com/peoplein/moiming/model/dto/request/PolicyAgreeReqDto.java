package com.peoplein.moiming.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.annotation.EnumConstraint;
import com.peoplein.moiming.domain.enums.PolicyType;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.domain.enums.PolicyType.*;

public class PolicyAgreeReqDto {

    @ApiModel(value = "Policy API - 요청 - 약관 동의 변경")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PolicyAgreeUpdateReqDto {

        @NotEmpty // 안에 값이 들어있고, Null 이 아닌지도 체크
        @Valid
        @JsonProperty("policies")
        private List<PolicyAgreeDto> policyDtos;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PolicyAgreeDto {
            @NotNull
            private Boolean hasAgreed;

            @NotNull
            @EnumConstraint(allowedValues = {MARKETING_SMS, MARKETING_EMAIL}, message = "필수약관 동의 여부는 변경할 수 없습니다")
            private PolicyType policyType;

        }

    }
}
