package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.Member;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@ApiModel(value = "Moim Post API - 응답 - 모임 특정 게시물 모든 정보와 댓글 정렬된 채로 전달")
@Getter
@Setter
@NoArgsConstructor
public class MoimPostDetailViewRespDto {

    private Long moimPostId;
    private String postTitle;
    private String postContent;
    private String postCategory;
    private boolean hasPrivateVisibility;
    private boolean hasFiles;
    private int commentCnt;
    private String createdAt;
    private String updatedAt;
    private MemberInfoDto postCreator;
    private List<ParentCommentDto> postComments;

    public MoimPostDetailViewRespDto(MoimPost moimPost, List<PostComment> parentComments, Map<Long, List<PostComment>> inputMap) {

        this.moimPostId = moimPost.getId();
        this.postTitle = moimPost.getPostTitle();
        this.postContent = moimPost.getPostContent();
        this.postCategory = moimPost.getMoimPostCategory().getValue();
        this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
        this.hasFiles = moimPost.isHasFiles();
        this.commentCnt = moimPost.getCommentCnt();
        this.createdAt = moimPost.getCreatedAt() + "";
        this.updatedAt = moimPost.getUpdatedAt() + "";
        this.postCreator = new MemberInfoDto(moimPost.getMember());
        this.postComments = parentComments.stream().map(parent -> new ParentCommentDto(parent, inputMap.get(parent.getId()))).collect(Collectors.toList());

    }


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
    public static class ParentCommentDto {

        private Long parentCommentId;
        private String content;
        private int depth;
        private boolean hasDeleted;
        private boolean reported;
        private String createdAt;
        private String updatedAt;
        private MemberInfoDto commentCreator;
        private List<ChildCommentDto> childComments;

        public ParentCommentDto(PostComment parentComment, List<PostComment> childComments) {
            this.parentCommentId = parentComment.getId();
            this.content = parentComment.getContent();
            this.depth = parentComment.getDepth();
            this.hasDeleted = parentComment.isHasDeleted();
            this.reported = parentComment.isReported();
            this.createdAt = parentComment.getCreatedAt() + "";
            this.updatedAt = parentComment.getUpdatedAt() + "";
            this.commentCreator = new MemberInfoDto(parentComment.getMember());
            this.childComments = childComments.stream().map(ChildCommentDto::new).collect(Collectors.toList());
        }

        @Getter
        @Setter
        public static class ChildCommentDto {

            private Long childCommentId;
            private String content;
            private int depth;
            private boolean hasDeleted;
            private boolean reported;
            private String createdAt;
            private String updatedAt;
            private Long parentId;
            private MemberInfoDto commentCreator;

            public ChildCommentDto(PostComment childComment) {
                this.childCommentId = childComment.getId();
                this.content = childComment.getContent();
                this.depth = childComment.getDepth();
                this.hasDeleted = childComment.isHasDeleted();
                this.reported = childComment.isReported();
                this.createdAt = childComment.getCreatedAt() + "";
                this.updatedAt = childComment.getUpdatedAt() + "";
                this.parentId = childComment.getParent().getId();
                this.commentCreator = new MemberInfoDto(childComment.getMember());
            }
        }

    }

}

