package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.model.dto.domain.PolicyAgreeDto;
import com.peoplein.moiming.model.dto.request.PolicyAgreeRequestDto;
import com.peoplein.moiming.model.dto.response.PolicyAgreeResponseDto;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PolicyAgreeService {

    private final int MOIMING_CUR_POLICY_REQUEST_COUNT = 6;
    private final PolicyAgreeRepository policyAgreeRepository;

    public void createUserPolicyAgree(Member signInMember, List<PolicyAgreeRequestDto> policyAgreeList) { // 초기 회원가입 로직 중 일부

        if (policyAgreeList.isEmpty()) {
            throw new RuntimeException("잘못된 형식입니다 - 약관 타입 없음");
        }

        if (policyAgreeList.size() != MOIMING_CUR_POLICY_REQUEST_COUNT) {
            throw new RuntimeException("전달 약관 갯수 부족 : " + policyAgreeList.size() + " 개 전달됨");
        }

        for (PolicyAgreeRequestDto policyAgreeDto : policyAgreeList) {
            PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(signInMember, policyAgreeDto.getPolicyType(), policyAgreeDto.isAgreed());
            policyAgreeRepository.save(policyAgree);
        }
    }

    /*
     전달된 내역들을 통해 요청한 유저의 (선택) 약관 내역들을 변경한다
     TODO:: 너무 길다 ㅋㅋㅋ.. Refactoring 필요
     */
    public PolicyAgreeResponseDto updatePolicyAgree(Member curMember, List<PolicyAgreeRequestDto> policyAgreeList) { // 수정로직

        if (policyAgreeList.isEmpty()) {
            throw new RuntimeException("잘못된 형식입니다 - 약관 타입 없음");
        }

        // 변경하려고 하는 내역중 필수 동의항목 내역이 있을경우 금한다
        boolean isAnyForbidden = policyAgreeList.stream().anyMatch(
                updatingRequest ->
                        updatingRequest.getPolicyType().equals(PolicyType.SERVICE) ||
                                updatingRequest.getPolicyType().equals(PolicyType.PRIVACY) ||
                                updatingRequest.getPolicyType().equals(PolicyType.AGE)
        );

        if (isAnyForbidden) {
            log.error("필수 약관 변경 요청 - 경로 확인 필요 : {}", curMember.getUid());
            throw new RuntimeException("잘못된 경로 및 요청 - 경로 확인 필요 : 필수 약관 동의 여부는 변경할 수 없습니다");
        }


        // 해당 멤버의 Policy Agree 정보를 찾는다
        // 비어있을 리는 없을 듯
        List<PolicyAgree> curMemberPolicyAgreedList = policyAgreeRepository.findByMemberId(curMember.getId());

        // TODO :: 이걸 레포지토리단에서 하는 건 괜찮을까?
        if (curMemberPolicyAgreedList.isEmpty()) {
            log.error("Member 약관 없음 - 확인 필요 : {}", curMember.getUid());
            throw new RuntimeException("잘못된 상황입니다 - Member 약관 확인 필요");
        }

        // MOIMING_CUR_POLICY_REQUEST_COUNT = N
        for (PolicyAgreeRequestDto updatingPolicy : policyAgreeList) {

            PolicyType updatingType = updatingPolicy.getPolicyType();

            Optional<PolicyAgree> findPolicy = curMemberPolicyAgreedList.stream().filter(curPolicy -> curPolicy.getPolicyType().equals(updatingType))
                    .findFirst();

            if (findPolicy.isPresent()) { // 여기에서 수정해는 내역이 기존 list 에 있다 - False  True 여부 확인하고 반대로 체킹

                PolicyAgree thisAgree = findPolicy.get();

                // 지금 요청하는 내역과 반대의 경우를 가지고 있다면, 바꿔준다.
                if (thisAgree.isAgreed() != updatingPolicy.isAgreed()) { // 수정한다
                    thisAgree.setAgreed(updatingPolicy.isAgreed());
                    thisAgree.setUpdatedAt(LocalDateTime.now());
                    thisAgree.changeUpdatedUid(curMember.getUid());
                }

            } else { // 없다 - 객체 생성 필요 (추가된 약관 항목으로 보임)

                log.info("없던 약관 생성 진행 - 추가된 약관 항목 추정");
                // 현재는 추가될 필요가 없음
                throw new RuntimeException("없던 약관이 추가될 수는 없습니다");

                // 필요시 주석 해제 후 사용
                //PolicyAgree addedPolicyAgreement = PolicyAgree.createPolicyAgree(curMember, updatingPolicy.getPolicyType(), updatingPolicy.isAgreed());
                //policyAgreeRepository.save(addedPolicyAgreement);
            }
        }

        return buildResponse(curMember, curMemberPolicyAgreedList);
    }

    public String deletePolicyAgree() { // 탈퇴로직?
        return "";
    }

    // 그냥 현재 유저의 Policy 정보를 모두 보내준다
    public PolicyAgreeResponseDto buildResponse(Member curMember, List<PolicyAgree> curPolicyAgrees) {
        List<PolicyAgreeDto> policyAgrees = new ArrayList<>();
        for (PolicyAgree curPolicyAgree : curPolicyAgrees) {
            policyAgrees.add(new PolicyAgreeDto(curPolicyAgree));
        }

        return new PolicyAgreeResponseDto(curMember.getUid(), policyAgrees);
    }
}
