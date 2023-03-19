package com.peoplein.moiming.repository.jpa;


import com.peoplein.moiming.domain.MoimReview;
import com.peoplein.moiming.domain.ReviewAnswer;
import com.peoplein.moiming.domain.fixed.ReviewQuestion;
import com.peoplein.moiming.repository.MoimReviewRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;


import static com.peoplein.moiming.domain.QMoimPost.moimPost;
import static com.peoplein.moiming.domain.QMoimReview.*;
import static com.peoplein.moiming.domain.QReviewAnswer.*;
import static com.peoplein.moiming.domain.fixed.QReviewQuestion.*;
import static com.peoplein.moiming.domain.fixed.QQuestionChoice.*;
import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;


@Repository
@RequiredArgsConstructor
public class MoimReviewJpaRepository implements MoimReviewRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(MoimReview moimReview) {
        em.persist(moimReview);
        return moimReview.getId();
    }

    @Override
    public MoimReview findById(Long moimReviewId) {
        return em.find(MoimReview.class, moimReviewId);
    }

    @Override
    public Optional<MoimReview> findOptionalWithMemberByMemberAndMoimId(Long memberId, Long moimId) {
        return Optional.ofNullable(queryFactory.selectFrom(moimReview)
                .join(moimReview.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimReview.member.id.eq(memberId),
                        moimReview.moim.id.eq(moimId))
                .fetchOne()
        );
    }

    @Override
    public Optional<MoimReview> findOptionalWithMemberById(Long moimReviewId) {
        return Optional.ofNullable(queryFactory.selectFrom(moimReview)
                .join(moimReview.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimReview.id.eq(moimReviewId)).fetchOne());
    }

    /*
     LIST JOIN 하면 데이터 뻥튀기 > distinct 필요 > PAGING 안됨
     > 엔간하면 Batch 옵션 걸고 그냥 batch 로 가져오기
     */
    @Override
    public List<MoimReview> findAllByMoimId(Long moimId) {
        return queryFactory.selectFrom(moimReview)
                .join(moimReview.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
//                .join(moimReview.reviewAnswers, reviewAnswer).fetchJoin()
                .where(moimReview.moim.id.eq(moimId)).fetch();
    }

    @Override
    public List<ReviewAnswer> findReviewAnswerByMoimReviewId(Long moimReviewId) {
        return queryFactory.selectFrom(reviewAnswer)
                .join(reviewAnswer.moimReview, moimReview).fetchJoin()
                .join(reviewAnswer.reviewQuestion, reviewQuestion).fetchJoin()
                .join(moimReview.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(reviewAnswer.moimReview.id.eq(moimReviewId)).fetch();
    }

    @Override
    public List<ReviewQuestion> findReviewQuestionByIds(List<Long> reviewQuestionIds) {
        return queryFactory.selectFrom(reviewQuestion).distinct().join(reviewQuestion.questionChoices, questionChoice).where(reviewQuestion.id.in(reviewQuestionIds)).fetch();
    }

    /*
     질문 및 선택지를 조회해서 반환한다
     distinct 쿼리 필요
     이걸 쓰거나 레이지 로딩의 자동 조회를 사용하면 된다.
     */
    @Override
    public List<ReviewQuestion> findAllReviewQuestions() {
        return queryFactory.selectFrom(reviewQuestion).distinct().join(reviewQuestion.questionChoices, questionChoice).fetchJoin().fetch();
    }


    @Override
    public void removeWithAllReviewAnswers(Long moimReviewId, List<Long> reviewAnswerIds) {
        queryFactory.delete(reviewAnswer).where(reviewAnswer.id.in(reviewAnswerIds)).execute();
        queryFactory.delete(moimReview).where(moimReview.id.eq(moimReviewId)).execute();
    }


}