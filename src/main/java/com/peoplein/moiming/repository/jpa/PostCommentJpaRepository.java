package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.peoplein.moiming.domain.QPostComment.*;
import static com.peoplein.moiming.domain.QMoimPost.*;
import static com.peoplein.moiming.domain.moim.QMoim.*;

@Repository
@RequiredArgsConstructor
public class PostCommentJpaRepository implements PostCommentRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;


    private void checkIllegalQueryParams(Object... objs) {
        for (Object obj : objs) {
            if (Objects.isNull(obj)) {
                throw new InvalidQueryParameterException("쿼리 파라미터는 NULL 일 수 없습니다");
            }
        }
    }


    /*
     특정 게시물의 모든 댓글 일괄 삭제
     > 게시물이 삭제되거나, 모임이 삭제될 때 진행된다
     > 이 때, FK 제약조건에 위배되지 않게 두 개의 쿼리가 발생해야 한다
     > 답글이 연관관계의 주인 > 답글들이 댓글의 FK 를 가지고 있음
     > 답글이 먼저 삭제되어야 한다
     */
    @Override
    public void removeAllByMoimPostId(Long moimPostId) {

        System.out.println("hello");
        queryFactory.delete(postComment)
                .where(postComment.moimPost.id.eq(moimPostId), postComment.depth.eq(1)).execute();

        System.out.println("hello");
        queryFactory.delete(postComment)
                .where(postComment.moimPost.id.eq(moimPostId)).execute();
        System.out.println("hello");
    }


    ////////////////////////
    // -- IN USE
    public void save(PostComment postComment) {
        em.persist(postComment);
    }

    @Override
    public Optional<PostComment> findById(Long postCommentId) {
        checkIllegalQueryParams(postCommentId);
        return Optional.ofNullable(em.find(PostComment.class, postCommentId));
    }

    @Override
    public Optional<PostComment> findWithMoimPostAndMoimById(Long commentId) {


        return Optional.ofNullable(queryFactory.selectFrom(postComment)
                .join(postComment.moimPost, moimPost).fetchJoin()
                .join(moimPost.moim, moim).fetchJoin()
                .where(postComment.id.eq(commentId))
                .fetchOne());

    }


    // 성능 고민이 되기는 한다. Comment Table 은 비교적 빨리 찰 것 같음
    // PENDING -- MoimPost 요청에서 사용될 쿼리
    public List<PostComment> findByMoimPostInHierarchyQuery(Long moimPostId) {

        /*
         Query : <부모부터 나열하는 순방향 전개로 진행> >> 아쉽지만 오라클에서만 지원한다고 한다
            select                *
            from                  Comment c
            where                 c.moim_post_id = {post_id}
            start with            c.parent = null
            connect by            prior c.id = c.parent_id // 자부순
            order siblings by     c.created_at; // 모든 댓글들은 depth 나열 외에는 작성순서로 나열한다
         */

        /*
        > 또다른 가능한 방법

        SELECT C.COMMENT_ID, C.CONTENT, C.DEPTH, C.POST_ID, NVL(C.PARENT_ID, C.COMMENT_ID) "ㅈㄹ"

        FROM COMM C

        LEFT OUTER JOIN COMM P ON C.PARENT_ID = P.COMMENT_ID

        ORDER BY "ㅈㄹ", DEPTH, COMMENT_ID;
        */

        checkIllegalQueryParams(moimPostId);
        List<Object[]> rawData = em.createQuery(
                        "SELECT c.id, c.content, c.depth, c.member, c.moimPost, COALESCE(c.parent, c.id) " +
                                "FROM PostComment c " +
                                "LEFT JOIN PostComment p " +
                                "ON c.parent.id = p.id " +
                                "WHERE c.moimPost.id = :moimPostId " +
                                "ORDER BY c.depth, c.createdAt", Object[].class)
                .setParameter("moimPostId", moimPostId).getResultList();

        return null;

    }

}
