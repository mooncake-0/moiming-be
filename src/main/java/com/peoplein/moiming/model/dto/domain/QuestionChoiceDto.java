package com.peoplein.moiming.model.dto.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionChoiceDto {

    private Long id;
    private String choiceInfo;
    private int choiceOrder;

    public QuestionChoiceDto(Long id, String choiceInfo, int choiceOrder) {
        this.id = id;
        this.choiceInfo = choiceInfo;
        this.choiceOrder = choiceOrder;
    }

}