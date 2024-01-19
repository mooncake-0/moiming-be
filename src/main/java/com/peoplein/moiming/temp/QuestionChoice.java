//package com.peoplein.moiming.domain.fixed;
//
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//
//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class QuestionChoice {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    @Column(name = "question_choice_id")
//    private Long id;
//
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "review_question_id")
//    private ReviewQuestion reviewQuestion;
//    private String choiceInfo;
//    private int choiceOrder;
//
//    public QuestionChoice(String choiceInfo, int choiceOrder, ReviewQuestion reviewQuestion) {
//        this.choiceInfo = choiceInfo;
//        this.choiceOrder = choiceOrder;
//
//        /*
//         연관관계 매핑
//         */
//        this.reviewQuestion = reviewQuestion;
//        this.reviewQuestion.getQuestionChoices().add(this);
//
//    }
//}