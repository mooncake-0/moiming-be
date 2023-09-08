package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MemberMoimCounter;
import com.peoplein.moiming.repository.MemberMoimCounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberMoimCounterService {

    private static final int RETRY_COUNT = 3;
    private final MemberMoimCounterRepository memberMoimCounterRepository;

    // 트랜잭션은 1개씩
    public void create(Long memberId, Long moimId, LocalDate visitDate) {
        // Named Lock get
        final String lockName = String.format("%d-%d", memberId, moimId);

        for (int i = 0; i < RETRY_COUNT; i++) {
            boolean lock = memberMoimCounterRepository.acquireLock(lockName);
            if (lock)
                break;
        }

        Optional<MemberMoimCounter> findInstance = memberMoimCounterRepository.findBy(memberId, moimId, visitDate);
        if (!findInstance.isEmpty()) {
            memberMoimCounterRepository.releaseLock(lockName);
            return;
        }

        MemberMoimCounter createdInstance = MemberMoimCounter.create(memberId, moimId, visitDate);
        memberMoimCounterRepository.save(createdInstance);

        // Named Lock release
        memberMoimCounterRepository.releaseLock(lockName);
    }
}
