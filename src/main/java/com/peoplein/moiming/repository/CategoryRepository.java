package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.enums.CategoryName;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    @Transactional
    void save(Category category);

    Optional<Category> findById(Long categoryId);

    Optional<Category> findByCategoryName(CategoryName categoryName);

    List<Category> findByCategoryNames(List<CategoryName> categoryNames);

}
