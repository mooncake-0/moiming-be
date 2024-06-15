package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.Moim;

import java.util.List;
import java.util.Optional;

public interface MoimRepository {

    void save(Moim moim);

    Optional<Moim> findById(Long moimId);

    Optional<Moim> findWithJoinRuleById(Long moimId);

    Optional<Moim> findWithMoimMemberAndMemberById(Long moimId);

    Optional<Moim> findWithJoinRuleAndCategoriesById(Long moimId);

    Optional<Moim> findWithActiveMoimMembersById(Long moimId);

    List<Moim> findNewMoims(int offset, int limit);

    void remove(Long moimId);
}
