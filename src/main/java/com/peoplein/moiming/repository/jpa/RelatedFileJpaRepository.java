package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.file.RelatedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class RelatedFileJpaRepository {

    private final EntityManager em;

    public void save(RelatedFile relatedFile) {
        em.persist(relatedFile);
    }


}
