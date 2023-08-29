package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.MoimMember;

import java.util.List;
import java.util.Optional;

public interface MoimMemberRepository {

    void save(MoimMember mmLinker);

    List<MoimMember> findByMemberId(Long memberId);

    List<MoimMember> findWithMoimByMemberId(Long memberId);

    Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMoimByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMemberInfoByMemberAndMoimId(Long memberId, Long moimId);

    MoimMember findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId);

    List<MoimMember> findWithMemberInfoAndMoimByMoimId(Long moimId);

    List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds);

    Optional<MoimMember> findOptionalWithMoimByMemberAndMoimId(Long memberId, Long moimId);

    void remove(MoimMember moimMember);
}
