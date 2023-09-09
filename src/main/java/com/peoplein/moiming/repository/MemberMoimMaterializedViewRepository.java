package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MemberMoimMaterializedView;

import java.util.List;

public interface MemberMoimMaterializedViewRepository {

    Long save(MemberMoimMaterializedView memberMoimMaterializedView);
    List<MemberMoimMaterializedView> findByMoimIds(List<Long> moimIds);
    List<MemberMoimMaterializedView> findAll();
    void deleteMViews(List<Long> moimIds);
}
