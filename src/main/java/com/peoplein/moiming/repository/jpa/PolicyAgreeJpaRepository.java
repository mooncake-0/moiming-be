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
import java.util.Optional;

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


    private void checkIllegalQueryParams(Object... objs) {
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

    @Override
    public Optional<PolicyAgree> findByMemberIdAndPolicyType(Long memberId, PolicyType policyType) {

        return Optional.ofNullable(queryFactory.selectFrom(policyAgree)
                .where(policyAgree.member.id.eq(memberId),
                        policyAgree.policyType.eq(policyType))
                .fetchOne());
    }

}
