package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.MoimMember;

import java.util.List;
import java.util.Optional;

public interface MoimMemberRepository {

    void save(MoimMember mmLinker);

    List<MoimMember> findByMemberId(Long memberId);

    List<MoimMember> findWithMoimAndCategoryByMemberId(Long memberId); // Moim 을 가져올 때 Category 까지 쭉 Fetch Join

    Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMemberInfoByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId);

    List<MoimMember> findWithMemberInfoAndMoimByMoimId(Long moimId);

    List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds);

    Optional<MoimMember> findOptionalWithMoimByMemberAndMoimId(Long memberId, Long moimId);

    void remove(MoimMember moimMember);
}
