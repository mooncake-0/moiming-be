package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;

import java.util.List;
import java.util.Optional;

public interface PolicyAgreeRepository {

    void save(PolicyAgree policyAgree);

    List<PolicyAgree> findByMemberId(Long memberId);

    Optional<PolicyAgree> findByMemberIdAndPolicyType(Long memberI, PolicyType policyType);
}
