package com.peoplein.moiming.repository.jpa.query;

import com.peoplein.moiming.domain.FileUpload;
import com.peoplein.moiming.domain.QFileUpload;
import com.peoplein.moiming.repository.FileUploadRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.peoplein.moiming.domain.QFileUpload.*;

@Repository
public class FileUploadJpaRepository implements FileUploadRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    public FileUploadJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
    }

    @Override
    public Long saveFile(FileUpload fileUpload) {
        em.persist(fileUpload);
        return fileUpload.getId();
    }

    @Override
    public List<FileUpload> findFileUploadByOwnerPk(Long pk) {
        return queryFactory
                .selectFrom(fileUpload)
                .where(fileUpload.id.eq(pk))
                .fetch();
    }

    @Override
    public void removeFiles(List<Long> fileIds) {
        queryFactory
                .delete(fileUpload)
                .where(fileUpload.id.in(fileIds))
                .execute();
    }
}
