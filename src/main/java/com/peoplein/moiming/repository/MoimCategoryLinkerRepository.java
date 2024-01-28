package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MoimCategoryLinker;

import java.util.List;

public interface MoimCategoryLinkerRepository {

    void save(MoimCategoryLinker mcLinker);

    List<MoimCategoryLinker> findWithCategoryByMoimId(List<Long> moimIds);

    void removeAllByMoimId(Long moimId);
}
