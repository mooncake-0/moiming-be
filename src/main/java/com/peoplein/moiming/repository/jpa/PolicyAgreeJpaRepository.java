package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.peoplein.moiming.domain.QPolicyAgree.*;
import static com.peoplein.moiming.domain.enums.PolicyType.*;

@Repository
@RequiredArgsConstructor
public class PolicyAgreeJpaRepository implements PolicyAgreeRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(PolicyAgree policyAgree) {
        em.persist(policyAgree);
    }


    private void checkIllegalQueryParams(Object ... objs) {
        for (Object obj : objs) {
            if (Objects.isNull(obj)) {
                throw new InvalidQueryParameterException("쿼리 파라미터는 NULL 일 수 없습니다");
            }
        }
    }


    // 특정 Member 에 따른 PolicyAgree 객체들을 반환한다
    public List<PolicyAgree> findByMemberId(Long memberId) {
        checkIllegalQueryParams(memberId);
        return em.createQuery(
                        "select p from PolicyAgree p where p.member.id = :memberId", PolicyAgree.class
                ).setParameter("memberId", memberId)
                .getResultList();

    }


    // Policy 란 Entity 의 필요성? PolicyAgree 을 통한 Member 와의 N:N 이 필요할까?
    public List<PolicyAgree> findByMemberIdAndPolicyTypes(Long memberId, List<PolicyType> policyTypes) {

        checkIllegalQueryParams(memberId, policyTypes);

        return queryFactory.selectFrom(policyAgree)
                .where(policyAgree.member.id.eq(memberId),
                        policyAgree.policyType.in(policyTypes))
                .fetch();

    }

}
