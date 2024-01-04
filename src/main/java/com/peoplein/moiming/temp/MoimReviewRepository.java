//package com.peoplein.moiming.temp;
//
//import com.peoplein.moiming.temp.MoimReview;
//import com.peoplein.moiming.temp.ReviewAnswer;
//import com.peoplein.moiming.domain.fixed.ReviewQuestion;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface MoimReviewRepository {
//
//    Long save(MoimReview moimReview);
//
//    MoimReview findById(Long moimReviewId);
//
//    Optional<MoimReview> findOptionalWithMemberByMemberAndMoimId(Long memberId, Long moimId);
//
//    Optional<MoimReview> findOptionalWithMemberById(Long moimReviewId);
//
//    List<MoimReview> findAllByMoimId(Long moimId);
//
//    List<ReviewAnswer> findReviewAnswerByMoimReviewId(Long moimReviewId);
//
//    List<ReviewQuestion> findReviewQuestionByIds(List<Long> reviewQuestionIds);
//
//    List<ReviewQuestion> findAllReviewQuestions();
//
//    /*
//         MoimReivew 를 삭제하려면 ReviewAnswers 들도 다같이 삭제되어야 한다.
//         같은 도메인 수준이므로 한 메서드에서 처리한다
//         */
//    void removeWithAllReviewAnswers(Long moimReviewId, List<Long> reviewAnswerIds);
//
//}
