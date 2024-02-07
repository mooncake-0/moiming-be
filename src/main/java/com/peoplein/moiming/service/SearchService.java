package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimSearchType;
import com.peoplein.moiming.domain.enums.OrderBy;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.MoimCategoryMapperDto;
import com.peoplein.moiming.repository.MoimCategoryLinkerRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.SearchJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.MoimSearchType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final MoimRepository moimRepository;
    private final SearchJpaRepository searchJpaRepository;
    private final MoimCategoryLinkerRepository categoryRepository;

    @Transactional
    public MoimCategoryMapperDto searchMoim(String keyword, String sortBy, String areaFilterVal, String categoryFilterVal, Long lastMoimId, int limit) {

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

        Moim lastSearchedMoim = null;
        if (lastMoimId != null) {
            lastSearchedMoim = moimRepository.findById(lastMoimId).orElseThrow(()->{
                log.error("{}, searchMoim :: {}", this.getClass().getName(), "(이전 조회 마지막 모임) 현재 커서인 모임을 찾을 수 없습니다");
                return new MoimingApiException(ExceptionValue.MOIM_NOT_FOUND);
            });
        }


        List<Moim> searchPagedMoims = searchJpaRepository.findMoimByDynamicSearchCondition(moimSearchType, keyword, areaFilters, categoryFilters, lastSearchedMoim, limit,orderBy);
        List<Long> moimIds = searchPagedMoims.stream().map(Moim::getId).collect(Collectors.toList());
        List<MoimCategoryLinker> categoryLinkers = categoryRepository.findWithCategoryByMoimIds(moimIds);


        return new MoimCategoryMapperDto(searchPagedMoims, categoryLinkers);

    }
}
