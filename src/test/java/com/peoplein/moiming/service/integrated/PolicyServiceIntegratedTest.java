package com.peoplein.moiming.service.integrated;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import com.peoplein.moiming.service.PolicyAgreeService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignUpReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.assertj.core.api.Assertions.assertThat;


// 함수적으로 예외가 발생하는 사항들은 Unit 에서 다 함
// 통합적으로 발생할 수 있는 문제에 대해서 생각해야 한다

@SpringBootTest
@Transactional
public class PolicyServiceIntegratedTest extends TestObjectCreator {

    @Autowired
    private PolicyAgreeService policyAgreeService;

    @Autowired
    private EntityManager em;

    @Autowired
    private PolicyAgreeRepository policyAgreeRepository;

    private Member testMember;

    @BeforeEach
    void be() {
        // given - su data - 위 signIn 함수를 쓰고 싶지만, 완전한 Test 분리를 위해 사용하지 않는다
        Role testRole = makeTestRole(RoleType.USER);

        testMember = makeTestMember(memberPhone, nickname, ci, testRole);// 저장할 Member 를 만든다.
        String preRefreshToken = createTestJwtToken(testMember, 2000);// 해당 멤버에 Refresh Token 을 저장해준다
        testMember.changeRefreshToken(preRefreshToken);

        em.persist(testRole);
        em.persist(testMember);

        em.flush();
        em.clear();

    }


    @Test
    void createPolicyAgree_shouldSaveMemberPolicyAgreeStatus_whenRightInfoPassed() {

        // given
        PolicyType[] policyTypes = {SERVICE, PRIVACY, AGE, MARKETING_AGREE, MARKETING_SMS, MARKETING_PUSH};
        boolean[] hasAgreeds = {true, true, true, true, false, true};
        List<PolicyAgreeDto> policyDtos = makePolicyReqDtoList(hasAgreeds, policyTypes);

        // when
        policyAgreeService.createPolicyAgree(testMember, policyDtos);

        // then - db verify
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberId(testMember.getId());
        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(SERVICE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(PRIVACY)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(AGE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_AGREE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_PUSH)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
        }
    }

    // 필수 약관 외 Case 는 단위테스트에서 완료

}
