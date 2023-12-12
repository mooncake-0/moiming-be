package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MoimPostRespDto {

    @ApiModel(value = "Moim Post API - 응답 - 게시물 생성")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimPostCreateRespDto {
        private Long moimPostId;
        private String postTitle;
        private String postContent;
        private MoimPostCategory moimPostCategory;
        private boolean hasPrivateVisibility;
        private boolean hasFiles;
        private int commentCnt;
        private String createdAt;

        @JsonProperty("memberInfo")
        private PostMemberDto postMemberDto;

        public MoimPostCreateRespDto(MoimPost moimPost, boolean creator) {
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.postMemberDto = new PostMemberDto(moimPost.getMember(), creator);
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;
            private boolean creator;

            // TODO :: PF IMG 관련
            public PostMemberDto(Member member, boolean creator) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.creator = creator;
            }
        }
    }


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
        @JsonProperty("memberInfo")
        private PostMemberDto postMemberDto;


        public MoimPostViewRespDto(MoimPost moimPost, boolean creator) {
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.updatedAt = moimPost.getUpdatedAt() + "";
            this.postMemberDto = new PostMemberDto(moimPost.getMember(), creator);
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;
            private boolean creator;

            // TODO :: PF IMG 관련
            public PostMemberDto(Member member, boolean creator) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.creator = creator;
            }
        }

    }


    @ApiModel(value = "Moim Post API - 응답 - 모임 특정 게시물 모든 정보")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimPostDetailViewRespDto {

    }
}
