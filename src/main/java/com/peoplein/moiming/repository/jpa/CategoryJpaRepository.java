package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.repository.CategoryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.fixed.QCategory.*;

@Repository
@RequiredArgsConstructor
public class CategoryJpaRepository implements CategoryRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;


    @Override
    public void save(Category category) {
        em.persist(category);
    }

    @Override
    public Optional<Category> findById(Long categoryId) {

        /*
         Query : select * from category c where c.category_id = {categoryId}
         */

        return Optional.ofNullable(queryFactory.selectFrom(category)
                .where(category.id.eq(categoryId))
                .fetchOne());
    }

    @Override
    public Optional<Category> findByCategoryName(CategoryName categoryName) {

        /*
         Query : select * from category c where c.category_name = {categoryName}
         */

        return Optional.ofNullable(queryFactory.selectFrom(category)
                .where(category.categoryName.eq(categoryName))
                .fetchOne());

    }

    @Override
    public List<Category> findByCategoryNames(List<CategoryName> categoryNames) {

        /*
         Query : select * from category c where c.category_name in {categoryNames}
         */

        return queryFactory.selectFrom(category)
                .where(category.categoryName.in(categoryNames))
                .fetch();
    }


    @Override
    public List<Category> findAllOrderByDepth() {
        return queryFactory.selectFrom(category)
                .orderBy(category.categoryDepth.asc())
                .fetch();
    }
}
