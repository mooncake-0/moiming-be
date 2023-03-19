package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.MoimPostCategory;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class MoimPostRequestDto {

    private Long moimId;
    private Long moimPostId;
    private String postTitle;
    private String postContent;
    private boolean isNotice;
    private MoimPostCategory moimPostCategory;

}
