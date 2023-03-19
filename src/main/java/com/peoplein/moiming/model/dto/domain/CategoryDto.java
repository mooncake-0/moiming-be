package com.peoplein.moiming.model.dto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.DomainChecker;
import com.peoplein.moiming.domain.enums.CategoryName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryDto {

    @JsonIgnore
    private Long moimId; // Query 용
    private int depth;
    private CategoryName categoryName;

    public CategoryDto(Long moimId, int depth, CategoryName categoryName) {
        this.moimId = moimId;
        this.depth = depth;
        this.categoryName = categoryName;
    }

    public CategoryDto(Category category) {
        /*
         Category 도 들어가려면 필수 DTO 로 분류
         */
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), category);
        this.depth = category.getCategoryDepth();
        this.categoryName = category.getCategoryName();
    }

}