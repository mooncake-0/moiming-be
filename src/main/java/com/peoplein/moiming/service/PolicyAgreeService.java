package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignUpReqDto;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignUpReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyAgreeService {

    private final PolicyAgreeRepository policyAgreeRepository;

    // 회원 가입시에만 사용됨
    @Transactional
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


    @Transactional(readOnly = true)
    public List<PolicyAgree> getPolicyAgree(Member member, boolean optionalOnly) {

        if (member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        List<PolicyAgree> memberPolicyAgree = policyAgreeRepository.findByMemberId(member.getId());

        if (memberPolicyAgree.isEmpty() || memberPolicyAgree.size() != PolicyAgree.CUR_MOIMING_REQ_POLICY_CNT) {
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        if (optionalOnly) {
            List<PolicyAgree> optionalPolicy = new ArrayList<>();
            for (PolicyAgree policyAgree : memberPolicyAgree) {
                if (policyAgree.getPolicyType().isOptional()) {
                    optionalPolicy.add(policyAgree);
                }
            }
            return optionalPolicy;
        }
        return memberPolicyAgree;
    }


    /*
     전달된 내역들을 통해 요청한 유저의 (선택) 약관 내역들을 변경한다
     */
    @Transactional
    public PolicyAgree updatePolicyAgree(Member member, PolicyAgreeUpdateReqDto requestDto) {

        if (member == null || requestDto == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }


        PolicyAgree policyAgree = policyAgreeRepository.findByMemberIdAndPolicyType(member.getId(), requestDto.getPolicyType()).orElseThrow(() -> {
            log.error("{}, updatePolicyAgree :: {}", this.getClass().getName(), "[" + member.getId() + ", " + requestDto.getPolicyType() + "] 로 조회된 결과가 없습니다");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        });

        policyAgree.changeHasAgreed(requestDto.getHasAgreed(), member.getId());

        return policyAgree;
    }


    public String deletePolicyAgree() { // 탈퇴로직?
        return "";
    }

}
