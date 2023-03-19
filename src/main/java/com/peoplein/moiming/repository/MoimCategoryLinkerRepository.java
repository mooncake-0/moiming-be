package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MoimCategoryLinker;

import java.util.List;

public interface MoimCategoryLinkerRepository {

    Long save(MoimCategoryLinker mcLinker);

    List<MoimCategoryLinker> findWithCategoryByMoimId(Long moimId);

    /*
         특정 모임의 모든 CategoryLinker 모두 삭제
         */
    Long removeAllByMoimId(Long moimId);
}
