package com.peoplein.moiming.repository;

import com.peoplein.moiming.temp.session.MemberSessionLinker;

import java.util.Optional;

public interface MemberSessionLinkerRepository {

    Long save(MemberSessionLinker memberSessionLinker);

    Optional<MemberSessionLinker> findOptionalByMemberAndSessionId(Long memberId, Long sessionId);

    void removeAll(Long moimSessionId);
}
