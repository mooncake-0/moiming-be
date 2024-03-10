package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.fixed.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class AppCategoryDto {
    private List<Category> parentCategories;
    private Map<Long, List<Category>> childCategoriesMap;

}
