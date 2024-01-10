package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;

import java.util.List;
import java.util.Optional;

public interface MoimMemberRepository {

    void save(MoimMember mmLinker);

    List<MoimMember> findWithMoimAndCategoryByMemberId(Long memberId); // Moim 을 가져올 때 Category 까지 쭉 Fetch Join

    Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMemberInfoByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId);

    List<MoimMember> findWithMemberInfoAndMoimByMoimId(Long moimId);



    Optional<MoimMember> findOptionalWithMoimByMemberAndMoimId(Long memberId, Long moimId);

    void remove(MoimMember moimMember);

    // IN - USE

    List<MoimMember> findWithMoimByMemberId(Long memberId);

    List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds);

    List<MoimMember> findMemberMoimsWithRuleAndCategoriesByConditionsPaged(Long memberId, boolean isActiveReq, boolean isManagerReq, Moim lastMoim, int limit);

    void removeAllByMoimId(Long moimId);

}
