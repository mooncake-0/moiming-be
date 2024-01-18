package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.fixed.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class MoimFixedValInnerDto {

    private AppCategoryDto categoryDto;
    private List<AreaValue> areaStates;

    public MoimFixedValInnerDto(AppCategoryDto categoryDto, List<AreaValue> areaState) {
        this.categoryDto = categoryDto;
        this.areaStates = areaState;
    }

    @Getter
    @AllArgsConstructor
    public static class AppCategoryDto {

        private List<Category> parentCategories;
        private Map<Long, List<Category>> childCategoriesMap;

    }

}