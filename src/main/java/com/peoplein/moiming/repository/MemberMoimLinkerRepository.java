package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MemberMoimLinker;

import java.util.List;
import java.util.Optional;

public interface MemberMoimLinkerRepository {

    Long save(MemberMoimLinker mmLinker);

    List<MemberMoimLinker> findByMemberId(Long memberId);

    List<MemberMoimLinker> findWithMoimByMemberId(Long memberId);

    MemberMoimLinker findByMemberAndMoimId(Long memberId, Long moimId);

    Optional<MemberMoimLinker> findOptionalByMemberAndMoimId(Long memberId, Long moimId);

    MemberMoimLinker findWithMoimByMemberAndMoimId(Long memberId, Long moimId);

    MemberMoimLinker findWithMemberInfoByMemberAndMoimId(Long memberId, Long moimId);

    MemberMoimLinker findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId);

    List<MemberMoimLinker> findWithMemberInfoAndMoimByMoimId(Long moimId);

    List<MemberMoimLinker> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds);

    Optional<MemberMoimLinker> findOptionalWithMoimByMemberAndMoimId(Long memberId, Long moimId);

    void remove(MemberMoimLinker memberMoimLinker);
}
