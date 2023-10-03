package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;

import java.util.List;

public interface PolicyAgreeRepository {

    void save(PolicyAgree policyAgree);

    List<PolicyAgree> findByMemberId(Long memberId);

    List<PolicyAgree> findByMemberIdAndPolicyTypes(Long memberId, List<PolicyType> policyTypes);
}
