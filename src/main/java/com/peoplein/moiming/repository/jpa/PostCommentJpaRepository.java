package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.domain.QPostComment.*;
import static com.peoplein.moiming.domain.QMoimPost.*;
import static com.peoplein.moiming.domain.QMoim.*;

@Repository
@RequiredArgsConstructor
public class PostCommentJpaRepository implements PostCommentRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public Long save(PostComment postComment) {
        em.persist(postComment);
        return postComment.getId();
    }

    @Override
    public PostComment findById(Long postCommentId) {
        return em.find(PostComment.class, postCommentId);
    }

    @Override
    public PostComment findWithMoimPostById(Long postCommentId) {
        return queryFactory.selectFrom(postComment)
                .join(postComment.moimPost, moimPost).fetchJoin()
                .where(postComment.id.eq(postCommentId))
                .fetchOne();
    }

    @Override
    public PostComment findWithMoimPostAndMoimById(Long postCommentId) {

        return queryFactory.selectFrom(postComment)
                .join(postComment.moimPost, moimPost).fetchJoin()
                .join(moimPost.moim, moim).fetchJoin()
                .where(postComment.id.eq(postCommentId))
                .fetchOne();
    }

    @Override
    public void remove(PostComment postComment) {
        em.remove(postComment);
    }

    /*
     특정 게시물의 모든 댓글 일괄 삭제
     */
    @Override
    public Long removeAllByMoimPostId(Long moimPostId) {
        JPADeleteClause clause = new JPADeleteClause(em, postComment);
        return clause.where(postComment.moimPost.id.eq(moimPostId)).execute();
    }

    @Override
    public void removeAllByMoimPostIds(List<Long> moimPostIds) {
        queryFactory.delete(postComment).where(postComment.moimPost.id.in(moimPostIds)).execute();
    }

}
