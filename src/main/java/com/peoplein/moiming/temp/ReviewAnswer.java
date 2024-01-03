package com.peoplein.moiming.temp;

import com.peoplein.moiming.domain.fixed.ReviewQuestion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

//@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewAnswer {

//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    @Column(name = "review_answer_id")
    private Long id;
    private int anwChoice; // 객관식에 대한 선택, 주관식일 경우 0
    private String anwText; // 주관식에 대한 답변 , NULLABLE

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "moim_review_id")
    private MoimReview moimReview;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "review_question_id")
    private ReviewQuestion reviewQuestion;

    public static ReviewAnswer createAnswer(int anwChoice, String anwText, MoimReview moimReview, ReviewQuestion reviewQuestion) {
        ReviewAnswer reviewAnswer = new ReviewAnswer(anwChoice, anwText, moimReview, reviewQuestion);
        return reviewAnswer;
    }

    private ReviewAnswer(int anwChoice, String anwText, MoimReview moimReview, ReviewQuestion reviewQuestion) {

        this.anwChoice = anwChoice;
        this.anwText = anwText;

        /*
         연관관계 매핑
         */
        this.moimReview = moimReview;
        this.reviewQuestion = reviewQuestion;

        /*
         연관관계 편의 지정
         */
        this.moimReview.getReviewAnswers().add(this);

    }

    public void setAnwChoice(int anwChoice) {
        this.anwChoice = anwChoice;
    }

    public void setAnwText(String anwText) {
        this.anwText = anwText;
    }

}