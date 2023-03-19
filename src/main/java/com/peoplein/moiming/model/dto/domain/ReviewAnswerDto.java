package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.model.dto.request.ReviewAnswerRequestDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAnswerDto {

    private Long reviewAnswerId;
    private int anwChoice;
    private String anwText;

    public ReviewAnswerDto(Long reviewAnswerId, int anwChoice, String anwText) {
        this.reviewAnswerId = reviewAnswerId;
        this.anwChoice = anwChoice;
        this.anwText = anwText;
    }

}
