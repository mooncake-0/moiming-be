package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;

import java.util.List;
import java.util.Optional;

public interface MoimMemberRepository {

    void save(MoimMember mmLinker);
    Optional<MoimMember> findByMemberAndMoimId(Long memberId, Long moimId);
    Optional<MoimMember> findWithMoimAndCategoriesByMemberAndMoimId(Long memberId, Long moimId);
    Optional<MoimMember> findWithMemberByMemberAndMoimId(Long memberId, Long moimId); // 조회되지 않은 Member 의 정보도 가져올 수 있다

    List<MoimMember> findWithMoimByMoimId(Long moimId); // Moim Id 만을 가지고 조회할 일이 있을 때, Moim 과 함께 조회한다 특정 모임의 모든 멤버 상태 조회
    List<MoimMember> findWithMoimByMemberId(Long memberId); // 그 멤버가 속한 모든 모임을 조회
    List<MoimMember> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds); // 특정 모임내에서 지정 멤버들의 상태 조회 (Post 세부조회 등에서 사용)
    List<MoimMember> findMemberMoimsWithCursorConditions(Long memberId, boolean isActiveReq, boolean isManagerReq, Moim lastMoim, int limit);
    void removeAllByMoimId(Long moimId);

}
