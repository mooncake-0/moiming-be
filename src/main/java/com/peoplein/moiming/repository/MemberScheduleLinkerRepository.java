package com.peoplein.moiming.repository;

import com.peoplein.moiming.temp.MemberScheduleLinker;

import java.util.List;

public interface MemberScheduleLinkerRepository {

    Long save(MemberScheduleLinker memberScheduleLinker);

    MemberScheduleLinker findByMemberAndScheduleId(Long memberId, Long scheduleId);

    MemberScheduleLinker findWithScheduleByMemberAndScheduleId(Long memberId, Long scheduleId);

    void remove(MemberScheduleLinker memberScheduleLinker);

    void removeAllByScheduleId(Long scheduleId);

    void removeAllByScheduleIds(List<Long> scheduleIds);

    List<MemberScheduleLinker> findMemberScheduleLatest5ByMemberId(Long memberId);

}
