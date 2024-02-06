package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.repository.SmsVerificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.Optional;

import static com.peoplein.moiming.domain.QSmsVerification.*;

@Repository
@RequiredArgsConstructor
public class SmsVerificationJpaRepository implements SmsVerificationRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(SmsVerification smsVerification) {
        em.persist(smsVerification);
        return smsVerification.getId();
    }

    @Override
    public Optional<SmsVerification> findById(Long smsVerificationId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(smsVerification)
                        .where(smsVerification.id.eq(smsVerificationId))
                        .fetchOne()
        );
    }

}
