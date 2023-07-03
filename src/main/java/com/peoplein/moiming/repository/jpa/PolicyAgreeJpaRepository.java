package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PolicyAgreeJpaRepository implements PolicyAgreeRepository {

    private final EntityManager em;

    @Override
    public void save(PolicyAgree policyAgree) {
        em.persist(policyAgree);
    }


    // 특정 Member 에 따른 PolicyAgree 객체들을 반환한다
    public List<PolicyAgree> findByMemberId(Long memberId) {
        return em.createQuery(
                        "select p from PolicyAgree p where p.member.id = :memberId", PolicyAgree.class
                ).setParameter("memberId", memberId)
                .getResultList();

    }
}
