package com.peoplein.moiming.domain.fixed;

import com.peoplein.moiming.domain.enums.QuestionName;
import com.peoplein.moiming.domain.enums.ReviewQuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "review_question_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private ReviewQuestionType reviewQuestionType;

    @Enumerated(value = EnumType.STRING)
    private QuestionName questionName;

    private String questionInfo;

    @OneToMany(mappedBy = "reviewQuestion", cascade = CascadeType.ALL)
    private List<QuestionChoice> questionChoices = new ArrayList<>();

    public ReviewQuestion(ReviewQuestionType reviewQuestionType, QuestionName questionName, String questionInfo) {

        this.reviewQuestionType = reviewQuestionType;
        this.questionName = questionName;
        this.questionInfo = questionInfo;
    }
}