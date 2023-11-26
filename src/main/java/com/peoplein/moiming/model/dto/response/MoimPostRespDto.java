package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class MoimPostRespDto {

    @ApiModel(value = "Moim Post API - 응답 - 모임 모든 게시물 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimPostViewRespDto {

        private Long moimPostId;
        private String postTitle;
        private String postContent;
        private MoimPostCategory moimPostCategory;
        private int commentCnt;
        private boolean hasPrivateVisibility;
        private boolean hasFiles;
        private String createdAt;
        private String updatedAt;

        public MoimPostViewRespDto(MoimPost moimPost) {
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.updatedAt = moimPost.getUpdatedAt() + "";

        }

    }


    @ApiModel(value = "Moim Post API - 응답 - 모임 특정 게시물 모든 정보")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimPostDetailViewRespDto {

    }
}
