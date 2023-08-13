package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.model.dto.domain.PolicyAgreeDto;
import com.peoplein.moiming.model.dto.request.PolicyAgreeRequestDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class PolicyAgreeResponseDto {

    private Long memberId;
    private List<PolicyAgreeDto> policyAgrees = new ArrayList<>();

    public PolicyAgreeResponseDto(Long memberId, List<PolicyAgreeDto> policyAgrees) {
        this.memberId = memberId;
        this.policyAgrees = policyAgrees;
    }


}
