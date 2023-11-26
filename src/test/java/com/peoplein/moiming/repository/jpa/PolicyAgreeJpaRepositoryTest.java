package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({RepositoryTestConfiguration.class, PolicyAgreeJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class PolicyAgreeJpaRepositoryTest extends TestObjectCreator {

    private Member testMember;

    @Autowired
    private EntityManager em;

    @Autowired
    private PolicyAgreeRepository policyAgreeRepository;


    @BeforeEach
    void be() {
        // Role 및 Member 저장
        Role testRole = makeTestRole(RoleType.USER);
        testMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        em.persist(testRole);
        em.persist(testMember);

        em.persist(PolicyAgree.createPolicyAgree(testMember, PRIVACY, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, SERVICE, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, AGE, true));
        em.persist(PolicyAgree.createPolicyAgree(testMember, MARKETING_SMS, false));
        em.persist(PolicyAgree.createPolicyAgree(testMember, MARKETING_EMAIL, true));

        em.flush();
        em.clear();

    }

    // 1. findByMemberId()
    // SUCCESS
    @Test
    void findByMemberId_shouldReturnAllPolicyAgree_whenRightInfoPassed() {

        // given
        // when
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberId(testMember.getId());

        // then
        assertThat(policyAgrees.size()).isEqualTo(5);
        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(PRIVACY)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(SERVICE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(AGE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_EMAIL)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
        }
    }

    // Exception:  memberId 빈 값
    @Test
    void findByMemberId_shouldThrowException_whenMemberIdNull_byInvalidQueryParameterException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> policyAgreeRepository.findByMemberId(null)).isInstanceOf(InvalidQueryParameterException.class);

    }

    // 원하는 값을 반환하지 못했을 때
    @Test
    void findByMemberId_shouldReturnEmptyList_whenNoPolicyAgreeFound() {

        // given
        // when
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberId(1231L);

        // then
        assertTrue(policyAgrees.isEmpty());
        assertNotNull(policyAgrees); // NULL 은 아님

    }

    // findByMemberAndPolicyTypes
    // 0개 빈 통 반환
    @Test
    void findByMemberIdAndPolicyTypes_shouldReturnEmptyList_whenZeroPolicyTypePassed() {

        // given
        List<PolicyType> queryPolicyTypes = new ArrayList<>();

        // when
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberIdAndPolicyTypes(testMember.getId(), queryPolicyTypes);

        // then
        assertThat(policyAgrees.size()).isEqualTo(0);

    }

    // 1개 성공
    @Test
    void findByMemberIdAndPolicyTypes_shouldReturnResult_whenOnePolicyTypePassed() {

        // given
        List<PolicyType> queryPolicyTypes = List.of(PRIVACY);

        // when
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberIdAndPolicyTypes(testMember.getId(), queryPolicyTypes);

        // then
        assertThat(policyAgrees.size()).isEqualTo(1);
        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(PRIVACY)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
        }
    }


    // 2개 성공
    @Test
    void findByMemberIdAndPolicyTypes_shouldReturnResult_whenTwoPolicyTypePassed() {

        // given
        List<PolicyType> queryPolicyTypes = List.of(MARKETING_SMS, MARKETING_EMAIL);

        // when
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberIdAndPolicyTypes(testMember.getId(), queryPolicyTypes);

        // then
        assertThat(policyAgrees.size()).isEqualTo(2);
        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_EMAIL)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
        }

    }

    // 3개 성공
    @Test
    void findByMemberIdAndPolicyTypes_shouldReturnResult_whenThreePolicyTypePassed() {

        // given
        List<PolicyType> queryPolicyTypes = List.of(SERVICE, MARKETING_SMS, MARKETING_EMAIL);

        // when
        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberIdAndPolicyTypes(testMember.getId(), queryPolicyTypes);

        // then
        assertThat(policyAgrees.size()).isEqualTo(3);
        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(SERVICE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_EMAIL)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
        }
    }

    // Member 찾을 수 없음
    @Test
    void findByMemberIdAndPolicyTypes_shouldReturnEmptyList_whenMemberNotFound() {

        // given
        List<PolicyType> queryPolicyTypes = List.of(MARKETING_SMS, MARKETING_EMAIL); // 가장 일반적일 상황

        // when
        List<PolicyAgree> policyTypes = policyAgreeRepository.findByMemberIdAndPolicyTypes(1234L, queryPolicyTypes);

        // then
        assertThat(policyTypes.size()).isEqualTo(0);

    }

    // Exception : member Id Null
    @Test
    void findByMemberIdAndPolicyTypes_shouldThrowException_whenMemberIdNull_byInvalidQueryParameterException() {

        // given
        List<PolicyType> queryPolicyTypes = List.of(MARKETING_SMS, MARKETING_EMAIL);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeRepository.findByMemberIdAndPolicyTypes(null, queryPolicyTypes)).isInstanceOf(InvalidQueryParameterException.class);

    }

    // Exception :  PolicyTypes Null
    @Test
    void findByMemberIdAndPolicyTypes_shouldThrowException_whenPolicyTypeListNull_byInvalidQueryParameterException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> policyAgreeRepository.findByMemberIdAndPolicyTypes(testMember.getId(), null)).isInstanceOf(InvalidQueryParameterException.class);

    }
}