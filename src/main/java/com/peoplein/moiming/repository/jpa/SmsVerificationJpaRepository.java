package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class SmsVerificationJpaRepository implements SmsVerificationRepository {

    private EntityManager em;
    private JPAQueryFactory queryFactory;


    @Override
    public Long save(SmsVerification smsVerification) {
        em.persist(smsVerification);
        return smsVerification.getId();
    }

}
