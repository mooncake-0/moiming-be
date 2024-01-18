package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;

import java.util.List;
import java.util.Optional;

public interface MoimRepository {

    void save(Moim moim);

    Optional<Moim> findById(Long moimId);

    Optional<Moim> findWithJoinRuleById(Long moimId);

    Optional<Moim> findWithMoimMembersById(Long moimId);

    List<Moim> findMoimBySearchCondition(List<String> keywordList, Area area, Category category);

    // MEMO :: NEW
    Optional<Moim> findWithJoinRuleAndCategoryById(Long moimId);

    void remove(Long moimId);
}
