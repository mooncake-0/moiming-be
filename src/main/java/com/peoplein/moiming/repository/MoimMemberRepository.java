package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;

import java.util.List;
import java.util.Optional;

public interface MoimMemberRepository {

    void save(MoimMember mmLinker);
    Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId);
    Optional<MoimMember> findWithMemberByMemberAndMoimId(Long memberId, Long moimId); // 조회되지 않은 Member 의 정보도 가져올 수 있다\
    List<MoimMember> findWithMoimByMemberId(Long memberId);
    List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds);
    List<MoimMember> findMemberMoimsWithRuleAndCategoriesByConditionsPaged(Long memberId, boolean isActiveReq, boolean isManagerReq, Moim lastMoim, int limit);
    void removeAllByMoimId(Long moimId);

}
