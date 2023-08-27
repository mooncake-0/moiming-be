package com.peoplein.moiming.model.dto.request_b;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostCommentRequestDto {

    private Long moimPostId; // 댓글 생성시 필요
    private Long commentId; // 댓글 수정시 필요
    private String commentContent;

}
