package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.QPostFile;
import com.peoplein.moiming.repository.PostFileRepository;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.domain.QPostComment.postComment;
import static com.peoplein.moiming.domain.QPostFile.*;

@Repository
public class PostFileJpaRepository implements PostFileRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final PostFileSpringJpaRepository jpaRepository;

    public PostFileJpaRepository(EntityManager em, JPAQueryFactory queryFactory, PostFileSpringJpaRepository jpaRepository) {
        this.em = em;
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }


    @Override
    public void removeWithMoimPostId(Long moimPostId) {
        queryFactory
                .delete(postFile)
                .where(postFile.moimPost.id.eq(moimPostId))
                .execute();
    }
}
