package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;

import java.util.List;
import java.util.Optional;

public interface MoimMemberRepository {

    void save(MoimMember mmLinker);
    Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId);
    Optional<MoimMember> findWithMoimByMemberAndMoimId(Long memberId, Long moimId);
    Optional<MoimMember> findWithMoimAndCategoriesByMemberAndMoimId(Long memberId, Long moimId);
    Optional<MoimMember> findWithMemberAndInfoByMemberAndMoimId(Long memberId, Long moimId); // 조회되지 않은 Member 의 정보도 가져올 수 있다
    List<MoimMember> findActiveWithMemberByMoimId(Long moimId);
    List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds); // 특정 모임내에서 지정 멤버들의 상태 조회 (Post 세부조회 등에서 사용)
    Optional<MoimMember> findLongestNormalActiveMemberByMoimId(Long moimId);
    List<MoimMember> findMemberMoimsWithCursorConditions(Long memberId, boolean isActiveReq, boolean isManagerReq, Moim lastMoim, int limit);
    List<MoimMember> findByMemberId(Long memberId);
    void removeAllByMoimId(Long moimId);

}
