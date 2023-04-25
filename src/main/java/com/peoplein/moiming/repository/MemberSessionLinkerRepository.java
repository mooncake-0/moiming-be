package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.session.MemberSessionLinker;

public interface MemberSessionLinkerRepository {

    Long save(MemberSessionLinker memberSessionLinker);

    void removeAll(Long moimSessionId);
}
