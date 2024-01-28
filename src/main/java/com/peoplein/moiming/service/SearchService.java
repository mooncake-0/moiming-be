package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimSearchType;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.repository.MoimCategoryLinkerRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.SearchJpaRepository;
import com.peoplein.moiming.repository.jpa.MoimJoinRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.MoimSearchType.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchJpaRepository searchJpaRepository;
    private final MoimCategoryLinkerRepository categoryRepository;

    @Transactional
    public Map<String, Object> searchMoim(String keyword, String sortBy, String areaFilterVal, String categoryFilterVal, int offset, int limit) {

        OrderBy orderBy = OrderBy.findOrderBy(sortBy);
        MoimSearchType moimSearchType = NO_FILTER;

        List<AreaValue> areaFilters;
        if (Objects.equals(areaFilterVal, "")) { // 필터가 안걸렸음 -> keyword 중에 지역이 존재하는지 확인
            areaFilters = AreaValue.consistsInArea(keyword);
        }else { // 필터가 걸려있음 // 이게 최우선 지역 조건
            areaFilters = new ArrayList<>(List.of(AreaValue.fromName(areaFilterVal)));
            moimSearchType = AREA_FILTER_ON;
        }

        List<CategoryName> categoryFilters;
        if (Objects.equals(categoryFilterVal, "")) { // 필터가 안걸렸음
            categoryFilters = CategoryName.consistsInCategoryName(keyword);
        } else { // 필터가 걸려있음
            categoryFilters = new ArrayList<>(List.of(CategoryName.fromValue(categoryFilterVal)));
            if (moimSearchType.equals(AREA_FILTER_ON)) {
                moimSearchType = BOTH_FILTER_ON;
            }else {
                moimSearchType = CATEGORY_FILTER_ON;
            }
        }

        List<Moim> searchPagedMoims = searchJpaRepository.findMoimByDynamicSearchCondition(moimSearchType, keyword, areaFilters, categoryFilters, offset, limit,orderBy);
        List<Long> moimIds = searchPagedMoims.stream().map(Moim::getId).collect(Collectors.toList());

        List<MoimCategoryLinker> categoryLinkers = categoryRepository.findWithCategoryByMoimId(moimIds);

        Map<String, Object> listMap = new HashMap<>();
        listMap.put("PAGED_MOIMS", searchPagedMoims);
        listMap.put("CATEGORIES", categoryLinkers);

        return listMap;
    }
}
