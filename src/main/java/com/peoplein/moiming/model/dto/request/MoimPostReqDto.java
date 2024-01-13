package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.MoimPostCategory;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MoimPostReqDto {

    @ApiModel(value = "Moim Post API - 요청 - 게시물 생성")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimPostCreateReqDto {

        @NotNull
        private Long moimId;

        @NotNull
        @Size(min = 5)
        private String postTitle;

        @NotNull
        @Size(min = 10)
        private String postContent;

        @NotNull
        private String moimPostCategory;

        @NotNull
        private Boolean hasFiles;

        @NotNull
        private Boolean hasPrivateVisibility;

    }


    @ApiModel(value = "Moim Post API - 요청 - 게시물 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimPostUpdateReqDto {

        @NotNull
        private Long moimPostId;

        @Size(min = 5)
        private String postTitle;

        @Size(min = 10)
        private String postContent;

        private String moimPostCategory;

        private Boolean hasFiles;

        private Boolean hasPrivateVisibility;

    }
}
