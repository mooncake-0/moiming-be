package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyAgreeDto {

    private PolicyType policyType;
    private boolean isAgreed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PolicyAgreeDto(PolicyAgree policyAgree) {
        this.policyType = policyAgree.getPolicyType();
        this.isAgreed = policyAgree.isAgreed();
        this.createdAt = policyAgree.getCreatedAt();
        this.updatedAt = policyAgree.getUpdatedAt();
    }

}
