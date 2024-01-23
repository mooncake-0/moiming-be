package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;

import java.util.Optional;

public interface MoimCountRepository {

    void save(MoimDailyCount dailyCount);
    void save(MoimMonthlyCount monthlyCount);
    Optional<MoimDailyCount> findDailyByMemberIdAndMoimIdAndCurrentDate(Long memberId, Long moimId);
    Optional<MoimMonthlyCount> findMonthlyByMemberIdAndMoimIdAndCurrentDate(Long moimId);
}
