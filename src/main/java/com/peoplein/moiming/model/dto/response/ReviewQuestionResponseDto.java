package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.model.dto.domain.QuestionChoiceDto;
import com.peoplein.moiming.model.dto.domain.ReviewQuestionDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQuestionResponseDto {

    private ReviewQuestionDto reviewQuestionDto;
    private List<QuestionChoiceDto> questionChoiceDtos;

    public ReviewQuestionResponseDto(ReviewQuestionDto reviewQuestionDto, List<QuestionChoiceDto> questionChoiceDtos) {

        this.reviewQuestionDto = reviewQuestionDto;
        this.questionChoiceDtos = questionChoiceDtos;

    }
}
