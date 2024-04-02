package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.file.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FileJpaRepository {

    private final EntityManager em;

    public void save(File file) {
        em.persist(file);
    }

    public Optional<File> findById(Long fileId) {
        return Optional.ofNullable(em.find(File.class, fileId));
    }

    // 최초에 유저가 저장한 순서가 createdAt 대로 저장되고, 같다면 id 순서
    public List<File> findByIds(List<Long> fileIds) {
        return em.createQuery("SELECT f FROM File f " +
                        "WHERE f.id IN :fileIds " +
                        "ORDER BY f.createdAt, f.id")
                .setParameter("fileIds", fileIds)
                .getResultList();

    }

    public void remove(File file) {
        em.remove(file);
    }


    // 굳이 조회하지 않고 삭제하는 로직도 있음
    public void removeById(Long fileId) {
        em.createQuery("DELETE FROM File f WHERE f.id = :fileId")
                .setParameter("fileId", fileId)
                .executeUpdate();
    }

}
