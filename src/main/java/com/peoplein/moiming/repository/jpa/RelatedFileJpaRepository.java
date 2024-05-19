package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.file.FileDomain;
import com.peoplein.moiming.domain.file.RelatedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RelatedFileJpaRepository {

    private final EntityManager em;

    public void save(RelatedFile relatedFile) {
        em.persist(relatedFile);
    }

    public List<RelatedFile> findWithFileByDomainId(Long domainId, FileDomain fileDomain) {
        return em.createQuery("SELECT rf FROM RelatedFile rf " +
                        "JOIN FETCH rf.file " +
                        "WHERE rf.domainId = :domainId " +
                        "AND rf.fileDomain = :fileDomain")
                .setParameter("domainId", domainId)
                .setParameter("fileDomain", fileDomain)
                .getResultList();
    }

    public List<RelatedFile> findWithFileByDomainIdOrderBySeq(Long domainId, FileDomain fileDomain) {
        return em.createQuery("SELECT rf FROM RelatedFile rf " +
                        "JOIN FETCH rf.file " +
                        "WHERE rf.domainId = :domainId " +
                        "AND rf.fileDomain = :fileDomain " +
                        "ORDER BY rf.sequence", RelatedFile.class)
                .setParameter("domainId", domainId)
                .setParameter("fileDomain", fileDomain)
                .getResultList();
    }


    public List<RelatedFile> findThumbnailFileByDomainIds(List<Long> domainIds, FileDomain fileDomain) {
        return em.createQuery("SELECT rf FROM RelatedFile  rf " +
                        "WHERE rf.domainId IN :domainIds " +
                        "AND rf.fileDomain = :fileDomain " +
                        "AND rf.sequence = 0", RelatedFile.class)
                .setParameter("domainIds", domainIds)
                .setParameter("fileDomain", fileDomain)
                .getResultList();
    }


    public void removeByFileIds(Set<Long> fileIds) {

        em.createQuery("DELETE FROM RelatedFile rf WHERE rf.file.id IN :fileIds")
                .setParameter("fileIds", fileIds)
                .executeUpdate();

        em.createQuery("DELETE FROM File f WHERE f.id IN :fileIds")
                .setParameter("fileIds", fileIds)
                .executeUpdate();

    }
}
