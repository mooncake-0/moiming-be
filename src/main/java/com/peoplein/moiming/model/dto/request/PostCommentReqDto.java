package com.peoplein.moiming.model.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PostCommentReqDto {

    @ApiModel(value = "Post Comment API - 요청 - 댓글 생성")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCommentCreateReqDto {

        @NotNull
        private Long postId;

        private Long parentId;

        @NotEmpty
        @Size(max = 100)
        private String content;

        @NotNull
        private Integer depth;

    }


    @ApiModel(value = "Post Comment API - 요청 - 댓글 수정 (내용만 수정가능)")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCommentUpdateReqDto {

        @NotNull
        private Long commentId;

        @NotNull
        @Size(max = 100)
        private String content;

    }
}