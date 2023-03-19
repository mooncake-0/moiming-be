package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.enums.CategoryName;

import java.util.List;

public interface CategoryRepository {

    Long save(Category category);

    Category findById(Long categoryId);

    Category findByCategoryName(CategoryName categoryName);

    List<Category> findByCategoryNames(List<CategoryName> categoryNames);

}
