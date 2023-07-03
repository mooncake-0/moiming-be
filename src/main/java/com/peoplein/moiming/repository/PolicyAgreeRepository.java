package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PolicyAgree;

import java.util.List;

public interface PolicyAgreeRepository {

    void save(PolicyAgree policyAgree);

    List<PolicyAgree> findByMemberId(Long memberId);
}
