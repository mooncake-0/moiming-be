package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignInReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PolicyAgreeService {

    private final PolicyAgreeRepository policyAgreeRepository;

    // 회원 가입시에만 사용됨
    public void createPolicyAgree(Member member, List<PolicyAgreeDto> policyDtos) {

        if (member == null || policyDtos == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (policyDtos.size() != PolicyAgree.CUR_MOIMING_REQ_POLICY_CNT) {
            throw new MoimingApiException(COMMON_REQUEST_VALIDATION);
        }

        for (PolicyAgreeDto reqDto : policyDtos) {
            PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, reqDto.getPolicyType(), reqDto.getHasAgreed());
            policyAgreeRepository.save(policyAgree);
        }
    }


    /*
     전달된 내역들을 통해 요청한 유저의 (선택) 약관 내역들을 변경한다
     */
    public void updatePolicyAgree(Member member, List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> policyDtos) {

        if (member == null || policyDtos == null || policyDtos.isEmpty()) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        List<PolicyType> policyTypes = policyDtos.stream().map(PolicyAgreeUpdateReqDto.PolicyAgreeDto::getPolicyType).collect(Collectors.toList());
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberIdAndPolicyTypes(member.getId(), policyTypes);

        if (policyTypes.size() != policyAgrees.size() || policyAgrees.isEmpty()) {
            log.error("{}, {}", "약관 수정 중 INTERNAL 오류 발생, C999", COMMON_INVALID_SITUATION.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        for (PolicyAgree policyAgree : policyAgrees) {
            for (PolicyAgreeUpdateReqDto.PolicyAgreeDto reqDto : policyDtos) {
                if (policyAgree.getPolicyType().equals(reqDto.getPolicyType())) {
                    policyAgree.changeHasAgreed(reqDto.getHasAgreed(), member.getId());
                }
            }
        }
    }


    public String deletePolicyAgree() { // 탈퇴로직?
        return "";
    }

}
