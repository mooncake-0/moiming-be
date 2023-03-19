package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.QuestionName;
import com.peoplein.moiming.domain.enums.ReviewQuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQuestionDto {

    private Long reviewQuestionId;
    private ReviewQuestionType reviewQuestionType;
    private QuestionName questionName;
    private String questionInfo;

    public ReviewQuestionDto(Long reviewQuestionId, ReviewQuestionType reviewQuestionType, QuestionName questionName, String questionInfo) {
        this.reviewQuestionId = reviewQuestionId;
        this.reviewQuestionType = reviewQuestionType;
        this.questionName = questionName;
        this.questionInfo = questionInfo;
    }

}