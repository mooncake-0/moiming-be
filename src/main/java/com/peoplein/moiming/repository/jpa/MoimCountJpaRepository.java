package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.query.QueryMoimSuggestMapDto;
import com.peoplein.moiming.repository.MoimCountRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
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


    // Object 로 가져온다음에 DTO 로 밖에서 묶어줄 예정
    // 즉, 모임의 모든 정보와, SUM 정보 (집계를 위함), MoimJoinRule 정보 까지 가져온다
    // 카테고리 id 는 따로 묶어서 지금 방식 그대로 진행
    @Override
    public List<QueryMoimSuggestMapDto> findMonthlyBySuggestedCondition(AreaValue areaFilter, CategoryName categoryFilter, List<LocalDate> givenDates, int offset, int limit) {

        // SELECT 문에서 대상 테이블에서 가져오면 자동으로 INNER JOIN 이 발생한다
        // LEFT OUTER JOIN 처럼 따로 관리가 되게 하려면 직접 JOIN 문을 만들고 알리아스만 주면 된다
        String jpql = "SELECT m, mjr, SUM(mmc.monthlyCount) AS totalCount " +
                "FROM MoimMonthlyCount mmc " +
                "INNER JOIN Moim m ON mmc.moim.id = m.id " +
                "LEFT OUTER JOIN MoimJoinRule mjr ON m.moimJoinRule.id = mjr.id " +
                "WHERE mmc.countDate IN :givenDates ";

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

        jpql += "GROUP BY mmc.moim.id " +
                "ORDER BY totalCount DESC, m.createdAt DESC, m.id DESC";

        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                .setParameter("givenDates", givenDates);

        if (areaFilter != null) { // 필터가 적용되었음
            query.setParameter("areaFilterVal", areaFilter.getName());
        }

        if (categoryFilter != null) {
            query.setParameter("categoryFilter", categoryFilter);
        }

        List<Object[]> resultList = query.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return convertToQueryDto(resultList);
    }


    public void removeAllByMoimId(Long moimId) {

        em.createQuery(
                        "DELETE FROM MoimDailyCount dc " +
                                "WHERE dc.moim.id = :moimId"
                ).setParameter("moimId", moimId)
                .executeUpdate();

        em.createQuery(
                        "DELETE FROM MoimMonthlyCount mc " +
                                "WHERE mc.moim.id = :moimId"
                ).setParameter("moimId", moimId)
                .executeUpdate();

    }



    private List<QueryMoimSuggestMapDto> convertToQueryDto(List<Object[]> rawList) {
        List<QueryMoimSuggestMapDto> queryDtoList = new ArrayList<>();
        for (Object[] rawObj : rawList) { // 결과가 없으면 LOOP 를 돌지 않는다
            queryDtoList.add(new QueryMoimSuggestMapDto(rawObj));
        }
        return queryDtoList;
    }
}
