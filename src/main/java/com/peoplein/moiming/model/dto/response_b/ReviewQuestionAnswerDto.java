package com.peoplein.moiming.model.dto.response_b;

import com.peoplein.moiming.model.dto.domain.ReviewAnswerDto;
import com.peoplein.moiming.model.dto.domain.ReviewQuestionDto;
import lombok.Getter;

@Getter
public class ReviewQuestionAnswerDto {

    private ReviewQuestionDto reviewQuestionDto;
    private ReviewAnswerDto reviewAnswerDto;

    public ReviewQuestionAnswerDto(ReviewQuestionDto reviewQuestionDto, ReviewAnswerDto reviewAnswerDto) {
        this.reviewQuestionDto = reviewQuestionDto;
        this.reviewAnswerDto = reviewAnswerDto;
    }
}
