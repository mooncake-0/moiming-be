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

    private String memberUid;
    private List<PolicyAgreeDto> policyAgrees = new ArrayList<>();

    public PolicyAgreeResponseDto(String memberUid, List<PolicyAgreeDto> policyAgrees) {
        this.memberUid = memberUid;
        this.policyAgrees = policyAgrees;
    }


}
