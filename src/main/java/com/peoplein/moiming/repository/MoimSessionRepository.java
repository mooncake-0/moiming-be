package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.session.MoimSession;

import java.util.List;
import java.util.Optional;

public interface MoimSessionRepository {

    Long save(MoimSession moimSession);
    Optional<MoimSession> findOptionalById(Long sessionId);
    List<MoimSession> findAllByMoimId(Long moimId);
}
