package com.peoplein.moiming.repository.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class MoimJoinRuleJpaRepository {

    private final EntityManager em;

    public void removeById(Long moimJoinRuleId) {
        em.createQuery(
                        "DELETE FROM MoimJoinRule mjr " +
                                "WHERE mjr.id = :moimJoinRuleId"
                ).setParameter("moimJoinRuleId", moimJoinRuleId)
                .executeUpdate();
    }

}
