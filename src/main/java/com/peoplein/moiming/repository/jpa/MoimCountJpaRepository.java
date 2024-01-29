package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimCountRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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

    @Override
    public List<MoimMonthlyCount> findMonthlyBySuggestedCondition(AreaValue areaFilter, CategoryName categoryFilter, LocalDate givenDate,int offset, int limit) {

        String jpql = "SELECT mc FROM MoimMonthlyCount mc " +
                "LEFT JOIN FETCH mc.moim m " +
                "LEFT JOIN FETCH m.moimJoinRule mjr " +
                "WHERE mc.countDate = :givenDate ";

        if (areaFilter != null) {
            String areaFilterCondition = "AND ";
            if (areaFilter.getDepth() == 0) { // 1차라면
                areaFilterCondition += "m.moimArea.state = :areaFilterVal ";
            } else {
                areaFilterCondition += "m.moimArea.city = :areaFilterVal ";
            }
            jpql += areaFilterCondition;
        }

        if (categoryFilter != null) {
            String categoryFilterCondition = "AND EXISTS (" +
                    "SELECT mcl FROM MoimCategoryLinker mcl " +
                    "JOIN Category c ON mcl.category.id = c.id " +
                    "WHERE mcl.moim.id = m.id " +
                    "AND c.categoryName = :categoryFilter" +
                    ") ";
            jpql += categoryFilterCondition;
        }

        jpql += "ORDER BY mc.monthlyCount DESC, m.createdAt DESC";

        TypedQuery<MoimMonthlyCount> query = em.createQuery(jpql, MoimMonthlyCount.class)
                .setParameter("givenDate", givenDate);

        if (areaFilter != null) { // 필터가 적용되었음
            query.setParameter("areaFilterVal", areaFilter.getName());
        }

        if (categoryFilter != null) {
            query.setParameter("categoryFilter", categoryFilter);
        }

        return query.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

}
