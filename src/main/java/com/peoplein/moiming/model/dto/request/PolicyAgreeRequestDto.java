package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.PolicyType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyAgreeRequestDto {

    private PolicyType policyType;
    private boolean isAgreed;

}
