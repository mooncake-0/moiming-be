package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.moim.Moim;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    @ApiModel(value = "Moim Post API - 응답 - 모임 특정 게시물 모든 정보와 댓글 정렬된 채로 전달")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimPostDetailViewRespDtoTemp {

        private Long moimPostId;
        private String postTitle;
        private String postContent;
        private String moimPostCategory;
        private boolean hasPrivateVisibility;
        private boolean hasFiles;
        private int commentCnt;
        private String createdAt;
        private String updatedAt;
        private Long updatedMemberId;

        @JsonProperty("postCreatorInfo")
        private MemberInfoDto postMemberDto;

        @JsonProperty("parentComments")
        @ApiModelProperty(notes = "댓글들, 최신순 정렬")
        private List<PostCommentInfoDto> parentCommentsDto;

        @JsonProperty("childComments")
        @ApiModelProperty(notes = "parentId 기준으로 정렬된 child 들이 묶임 (Map 자체는 정렬X)")
        private Map<Long, List<PostCommentInfoDto>> childCommentsDto;

        @Getter
        @Setter
        public static class MemberInfoDto {
            private Long memberId;
            private String nickname;

            // TODO :: PF IMG 관련
            public MemberInfoDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
            }
        }


        @Getter
        @Setter
        public static class PostCommentInfoDto {

            private Long postCommentId;
            private String content;
            private int depth;
            private boolean hasDeleted;
            private boolean reported;
            private Long updaterId;
            private String createdAt;
            private String updatedAt;
            private Long parentId;
            @JsonProperty("commentCreatorInfo")
            private MemberInfoDto commentMemberDto;

            public PostCommentInfoDto(PostComment postComment) {
                this.postCommentId = postComment.getId();
                this.content = postComment.getContent();
                this.depth = postComment.getDepth();
                this.hasDeleted = postComment.isHasDeleted();
                this.reported = postComment.isHasDeleted();
                this.updaterId = postComment.getUpdaterId();
                this.createdAt = postComment.getCreatedAt() + "";
                this.updatedAt = postComment.getUpdatedAt() + "";
                if (postComment.getParent() != null) {
                    this.parentId = postComment.getParent().getId();
                }
                this.commentMemberDto = new MemberInfoDto(postComment.getMember());
            }
        }

        public MoimPostDetailViewRespDtoTemp(MoimPost moimPost, List<PostComment> parentComments, Map<Long, List<PostComment>> inputmap) {
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory().getValue();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.commentCnt = moimPost.getCommentCnt();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.updatedAt = moimPost.getUpdatedAt() + "";
            this.updatedMemberId = moimPost.getUpdatedMemberId();
            this.postMemberDto = new MemberInfoDto(moimPost.getMember());
            this.parentCommentsDto = parentComments.stream().map(PostCommentInfoDto::new).collect(Collectors.toList());
            this.childCommentsDto = convertMapToDto(inputmap);

        }

        public Map<Long, List<PostCommentInfoDto>> convertMapToDto(Map<Long, List<PostComment>> inputmap) {

            Map<Long, List<PostCommentInfoDto>> childCommentsDto = new HashMap<>();
            for (Long parentId : inputmap.keySet()) {
                List<PostCommentInfoDto> childComments = inputmap.get(parentId).stream().map(PostCommentInfoDto::new).collect(Collectors.toList());
                childCommentsDto.put(parentId, childComments);
            }

            return childCommentsDto;
        }
    }


    @ApiModel(value = "Moim Post API - 응답 - 게시물 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoimPostUpdateRespDto {

        private Long moimPostId;
        private String postTitle;
        private String postContent;
        private String moimPostCategory;
        private boolean hasPrivateVisibility;
        private boolean hasFiles;
        private int commentCnt;
        private String createdAt;
        private String updatedAt;

        @JsonProperty("memberInfo")
        private PostMemberDto postMemberDto;

        public MoimPostUpdateRespDto(MoimPost moimPost) {

            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory().getValue();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.updatedAt = moimPost.getUpdatedAt() + "";
            this.postMemberDto = new PostMemberDto(moimPost.getMember());
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;

            // TODO :: PF IMG 관련
            public PostMemberDto(Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
            }
        }
    }
}
