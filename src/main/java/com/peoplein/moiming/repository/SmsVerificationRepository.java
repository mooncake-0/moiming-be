package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.SmsVerification;

import java.util.Optional;

public interface SmsVerificationRepository {

    Long save(SmsVerification smsVerification);

    Optional<SmsVerification> findOptionalById(Long id);

}
