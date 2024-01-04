package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;



    // String Category Value --> Category 로 최종적으로 반환해준다
    public List<Category> generateCategoryList(List<String> categoryNameValues) {

        if (categoryNameValues == null || categoryNameValues.isEmpty()) {
            return new ArrayList<Category>();
        }

        List<CategoryName> queryList = categoryNameValues.stream().map(CategoryName::fromValue).collect(Collectors.toList());
        List<Category> categories = categoryRepository.findByCategoryNames(queryList);

        validateCategories(categories);

        return categories;
    }



    private void validateCategories(List<Category> categories) {
        // Category 검증
        Category parentCategory = null;
        Category childCategory = null;

        for (Category category : categories) { // 카테고리 상하관계 분석
            if (category.getCategoryDepth() == 1) {
                parentCategory = category;
            } else {
                childCategory = category;
            }
        }

        // 검증 -> 1. 둘 중 하나라도 없으면 Exception   2. 둘이 종속관계가 아니면 Exception
        if (Objects.isNull(parentCategory) || Objects.isNull(childCategory) || !Objects.equals(childCategory.getParent().getCategoryName(), parentCategory.getCategoryName())) {
            log.error("{}, {}", "잘못된 관계의 카테고리가 매핑되었습니다, C999", COMMON_INVALID_SITUATION.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }
    }
}