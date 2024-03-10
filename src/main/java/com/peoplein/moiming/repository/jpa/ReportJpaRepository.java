package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Report;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.Temperature;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class ReportJpaRepository {

    private final EntityManager em;

    public void save(Report report) {
        em.persist(report);

    }
}
