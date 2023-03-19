package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.model.dto.domain.ReviewAnswerDto;
import com.peoplein.moiming.model.dto.domain.ReviewQuestionDto;
import lombok.Getter;

@Getter
public class ReviewAnswerRequestDto {

    private ReviewQuestionDto reviewQuestionDto;
    private ReviewAnswerDto reviewAnswerDto;

}
