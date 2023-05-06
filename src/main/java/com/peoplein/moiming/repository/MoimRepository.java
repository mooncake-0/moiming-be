package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;

import java.util.List;
import java.util.Optional;

public interface MoimRepository {

    Long save(Moim moim);

    Moim findById(Long moimId);
    Optional<Moim> findOptionalById(Long moimId);

    Moim findWithRulesById(Long moimId);

    void remove(Moim moim);

    List<Moim> findMoimBySearchCondition(List<String> keywordList, Area area, Category category);

    List<Moim> findAllMoim();
}
