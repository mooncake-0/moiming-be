package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MemberMoimCounter;

import java.time.LocalDate;
import java.util.Optional;

public interface MemberMoimCounterRepository {

    void save(MemberMoimCounter memberMoimCounter);
    Optional<MemberMoimCounter> findBy(Long memberId, Long moimId, LocalDate date);
    boolean acquireLock(String lockName);
    void releaseLock(String lockName);

}