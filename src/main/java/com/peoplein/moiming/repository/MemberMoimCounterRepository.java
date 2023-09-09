package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MemberMoimCounter;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberMoimCounterRepository {

    void save(MemberMoimCounter memberMoimCounter);
    Optional<MemberMoimCounter> findBy(Long memberId, Long moimId, LocalDate date);
    boolean acquireLock(String lockName);
    void releaseLock(String lockName);
    List<MoimViewCountTuple> findByGroupByMoimId();
    void deleteByMoimIds(List<Long> moimIds);

    @Getter
    class MoimViewCountTuple {
        private final Long moimId;
        private final Long viewCount;

        public MoimViewCountTuple(Long moimId, Long viewCount) {
            this.moimId = moimId;
            this.viewCount = viewCount;
        }
    }

}