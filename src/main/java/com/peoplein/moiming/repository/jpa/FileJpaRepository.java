package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.file.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

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
