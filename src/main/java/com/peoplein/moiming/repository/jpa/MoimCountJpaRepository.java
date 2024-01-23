package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.repository.MoimCountRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;


import static com.peoplein.moiming.domain.moim.QMoimDailyCount.*;
import static com.peoplein.moiming.domain.moim.QMoimMonthlyCount.*;
import static com.peoplein.moiming.domain.moim.QMoim.*;

@Repository
@RequiredArgsConstructor
public class MoimCountJpaRepository implements MoimCountRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    @Override
    public void save(MoimDailyCount dailyCount) {
        em.persist(dailyCount);
    }

    @Override
    public void save(MoimMonthlyCount monthlyCount) {
        em.persist(monthlyCount);
    }

    @Override
    public Optional<MoimDailyCount> findDailyByMemberIdAndMoimIdAndCurrentDate(Long memberId, Long moimId) {

        LocalDate today = LocalDate.now();

        return Optional.ofNullable(queryFactory.selectFrom(moimDailyCount)
                .where(moimDailyCount.member.id.eq(memberId)
                        , moimDailyCount.moim.id.eq(moimId)
                        , moimDailyCount.accessDate.eq(today))
                .fetchOne());

    }

    @Override
    public Optional<MoimMonthlyCount> findMonthlyByMemberIdAndMoimIdAndCurrentDate(Long moimId) {
        LocalDate currentYearMonth = LocalDate.now();
        currentYearMonth = currentYearMonth.withDayOfMonth(1);

        return Optional.ofNullable(queryFactory.selectFrom(moimMonthlyCount)
                .where(moimMonthlyCount.moim.id.eq(moimId)
                        , moimMonthlyCount.countDate.eq(currentYearMonth))
                .fetchOne());
    }
}
