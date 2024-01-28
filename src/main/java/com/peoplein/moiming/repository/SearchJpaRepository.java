package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimSearchType;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.MoimSearchType.*;
import static com.peoplein.moiming.domain.moim.QMoim.moim;

@Repository
@AllArgsConstructor
public class SearchJpaRepository {

    private final EntityManager em;


    //
    public List<Moim> findMoimByDynamicSearchCondition(MoimSearchType type, String keyword, List<AreaValue> areaFilters, List<CategoryName> categoryFilters, int offset, int limit, OrderBy orderBy) {

        String areaLogicOp = "";
        String categoryLogicOp = "";

        if (type.equals(NO_FILTER)) {
            areaLogicOp = "OR";
            categoryLogicOp = "OR";
        } else if (type.equals(AREA_FILTER_ON)) { // (keyword OR category) AND area
            areaLogicOp = "AND";
            categoryLogicOp = "OR";
        } else if (type.equals(CATEGORY_FILTER_ON)) { // (keyword OR area) AND category
            areaLogicOp = "OR";
            categoryLogicOp = "AND";
        } else { // BOTH_FILTER_ON
            areaLogicOp = "AND";
            categoryLogicOp = "AND";
        }

        String areaConditionBuilder = "";
        if (!areaFilters.isEmpty()) { // 지역에 대한 탐색이 들어간 경우만 추가한다
            String areaParam = "m.moimArea.city";
            if (areaFilters.size() == 1) {
                if (areaFilters.get(0).getDepth() == 0) { // 시 전체에 대한 검색이라면
                    areaParam = "m.moimArea.state";
                }
            }
            areaConditionBuilder += areaLogicOp + " " + areaParam + " IN :areaFilterVals ";
        }

        String categoryConditionBuilder = "";
        if (!categoryFilters.isEmpty()) { // 카테고리에 대한 탐색이 들어간 경우만 추가
            String adder = " EXISTS (" +
                    "SELECT mcl FROM MoimCategoryLinker mcl " +
                    "JOIN Category c ON mcl.category.id = c.id " +
                    "WHERE mcl.moim.id = m.id " +
                    "AND c.categoryName IN :categoryFilters" +
                    ") ";
            categoryConditionBuilder += categoryLogicOp + adder;
        }

        String jpql = "SELECT m FROM Moim m " +
                "LEFT JOIN FETCH m.moimJoinRule ";

        if (type.equals(BOTH_FILTER_ON) || type.equals(NO_FILTER)) { // 그냥 병렬로 하면 됨
            jpql += "WHERE m.moimName LIKE :keyword " +
                    areaConditionBuilder +
                    categoryConditionBuilder;
        } else {
            if (type.equals(AREA_FILTER_ON)) { // (keyword OR category) AND area
                jpql += "WHERE (m.moimName LIKE :keyword " +
                        categoryConditionBuilder +
                        ")" +
                        areaConditionBuilder;
            } else { // (keyword OR area) AND category
                jpql += "WHERE (m.moimName LIKE :keyword " +
                        areaConditionBuilder +
                        ")" +
                        categoryConditionBuilder
                ;
            }
        }

        jpql += getOrderBy(orderBy);

        List<String> areaFilterVals = areaFilters.stream().map(AreaValue::getName).collect(Collectors.toList());

        TypedQuery<Moim> query = em.createQuery(jpql, Moim.class)
                .setParameter("keyword", "%" + keyword + "%");

        if (StringUtils.hasText(areaConditionBuilder)) {
            query.setParameter("areaFilterVals", areaFilterVals);
        }

        if (StringUtils.hasText(categoryConditionBuilder)) {
            query.setParameter("categoryFilters", categoryFilters);
        }

        return query.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

    }


    public String getOrderBy(OrderBy orderBy) {
        if (orderBy == OrderBy.date) {
            return "ORDER BY m.createdAt DESC";
        } else {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
        }
    }

}
