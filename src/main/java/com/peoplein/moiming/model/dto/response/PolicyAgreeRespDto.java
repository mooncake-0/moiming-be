package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.annotation.EnumConstraint;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.PolicyType.*;

public class PolicyAgreeRespDto {


    @ApiModel(value = "Policy API - 요청 - 약관 수정 성공")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PolicyAgreeUpdateRespDto {
        private boolean hasAgreed;
        private PolicyType policyType;

        public PolicyAgreeUpdateRespDto(PolicyAgree policyAgree) {
            this.hasAgreed = policyAgree.isHasAgreed();
            this.policyType = policyAgree.getPolicyType();
        }
    }



    @ApiModel(value = "Policy API - 요청 - 선택 약관 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PolicyAgreeViewRespDto {

        private List<PolicyAgreeDto> policyAgrees;

        public PolicyAgreeViewRespDto(List<PolicyAgree> policyAgrees) {
            this.policyAgrees = policyAgrees.stream().map(PolicyAgreeDto::new).collect(Collectors.toList());
        }


        @Getter
        @Setter
        public class PolicyAgreeDto{
            private boolean hasAgreed;
            private PolicyType policyType;

            public PolicyAgreeDto(PolicyAgree policyAgree) {
                this.hasAgreed = policyAgree.isHasAgreed();
                this.policyType = policyAgree.getPolicyType();
            }
        }
    }
}
