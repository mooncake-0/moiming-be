package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.model.query.QueryMoimSuggestMapDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoimCountRepository {

    void save(MoimDailyCount dailyCount);

    void save(MoimMonthlyCount monthlyCount);

    Optional<MoimDailyCount> findDailyByMemberIdAndMoimIdAndCurrentDate(Long memberId, Long moimId);

    Optional<MoimMonthlyCount> findMonthlyByMemberIdAndMoimIdAndCurrentDate(Long moimId);

    List<QueryMoimSuggestMapDto> findMonthlyBySuggestedCondition(AreaValue areaFilter, CategoryName categoryFilter, List<LocalDate> givenDates, int offset, int limit);

    void removeAllByMoimId(Long moimId);
}
