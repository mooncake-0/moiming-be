package com.peoplein.moiming.model.dto.request_b;

import lombok.Getter;

import java.util.List;


@Getter
public class MoimReviewRequestDto {
    private Long moimId;
    private Long moimReviewId;
    private List<ReviewAnswerRequestDto> reviewAnswerRequestDtos;

}
