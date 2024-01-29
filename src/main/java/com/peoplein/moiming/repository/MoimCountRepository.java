package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoimCountRepository {

    void save(MoimDailyCount dailyCount);

    void save(MoimMonthlyCount monthlyCount);

    Optional<MoimDailyCount> findDailyByMemberIdAndMoimIdAndCurrentDate(Long memberId, Long moimId);

    Optional<MoimMonthlyCount> findMonthlyByMemberIdAndMoimIdAndCurrentDate(Long moimId);

    List<MoimMonthlyCount> findMonthlyBySuggestedCondition(AreaValue areaFilter, CategoryName categoryFilter, LocalDate givenDate, int offset, int limit);
}
