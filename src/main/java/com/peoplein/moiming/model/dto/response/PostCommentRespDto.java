package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.PostComment;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PostCommentRespDto {

    @ApiModel(value = "Post Comment API - 응답 - 댓글 생성")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PostCommentCreateRespDto {

        private Long commentId;
        private String content;
        private Integer depth;
        private Long parentId;
        private String createdAt;
        @JsonProperty("memberInfo")
        private CommentMemberDto commentMemberDto;

        @Getter
        @Setter
        public static class CommentMemberDto {

            private Long memberId;
            private String nickname;

            //TODO :: PF IMG 관련

            public CommentMemberDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                // TODO ::
            }
        }

        public PostCommentCreateRespDto(PostComment postComment) {
            this.commentId = postComment.getId();
            this.content = postComment.getContent();
            this.depth = postComment.getDepth();
            if (postComment.getParent() != null) this.parentId = postComment.getParent().getId();
            else this.parentId = null;
            this.createdAt = postComment.getCreatedAt() + "";
            this.commentMemberDto = new CommentMemberDto(postComment.getMember());
        }
    }


    @ApiModel(value = "Post Comment API - 응답 - 댓글 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PostCommentUpdateRespDto {
        private Long commentId;
        private String content;
        private Integer depth;
        private Long parentId;
        private String createdAt;
        private String updatedAt;
        private Long updaterId;
        @JsonProperty("memberInfo")
        private CommentMemberDto commentMemberDto;

        @Getter
        @Setter
        public static class CommentMemberDto {

            private Long memberId;
            private String nickname;

            //TODO :: PF IMG 관련

            public CommentMemberDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                // TODO ::
            }
        }

        public PostCommentUpdateRespDto(PostComment postComment) {
            this.commentId = postComment.getId();
            this.content = postComment.getContent();
            this.depth = postComment.getDepth();
            if (postComment.getParent() != null) this.parentId = postComment.getParent().getId();
            else this.parentId = null;
            this.updaterId = postComment.getUpdaterId();
            this.createdAt = postComment.getCreatedAt() + "";
            this.updatedAt = postComment.getUpdatedAt() + "";
            this.commentMemberDto = new CommentMemberDto(postComment.getMember());
        }
    }
}
