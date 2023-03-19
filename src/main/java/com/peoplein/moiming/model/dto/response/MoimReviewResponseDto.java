package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MoimReviewResponseDto {

    private Long moimReviewId;
    private MoimMemberInfoDto moimMemberInfoDto;
    private List<ReviewQuestionAnswerDto> moimReviewQuestionAnswerDto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
