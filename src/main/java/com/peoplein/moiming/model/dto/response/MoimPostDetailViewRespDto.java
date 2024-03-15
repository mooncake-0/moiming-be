package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.MoimingApiException;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;


@Slf4j
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

        Moim moim = moimPost.getMoim();
        if (moim == null) {
            log.warn("{}, MoimPostCreateRespDto Creator :: {}", this.getClass().getName(), "Moim 이 연관관계되어 있지 않음");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }
        this.postCreator = new MemberInfoDto(moim.getCreatorId(), moimPost.getMember());
        this.postComments = parentComments.stream().map(parent -> new ParentCommentDto(moim.getCreatorId(), parent, inputMap.get(parent.getId()))).collect(Collectors.toList());

    }


    @Getter
    @Setter
    public static class MemberInfoDto {

        private Long memberId;
        private String nickname;
        private String memberPfImgUrl;
        private boolean creator;

        public MemberInfoDto(Long moimCreatorId, Member member) {
            this.memberId = member.getId();
            this.nickname = member.getNickname();
            this.creator = Objects.equals(moimCreatorId, member.getId());
            this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
            if (member.getMemberInfo() != null) { // NULL 인 경우는 DORMANT 혹은 DELETED
                if (StringUtils.hasText(member.getMemberInfo().getPfImgUrl())) {
                    this.memberPfImgUrl = member.getMemberInfo().getPfImgUrl();
                }
            }
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

        public ParentCommentDto(Long moimCreatorId, PostComment parentComment, List<PostComment> childComments) {
            this.parentCommentId = parentComment.getId();
            this.content = parentComment.getContent();
            this.depth = parentComment.getDepth();
            this.hasDeleted = parentComment.isHasDeleted();
            this.reported = parentComment.isReported();
            this.createdAt = parentComment.getCreatedAt() + "";
            this.updatedAt = parentComment.getUpdatedAt() + "";
            this.commentCreator = new MemberInfoDto(moimCreatorId, parentComment.getMember());
            this.childComments = childComments.stream().map(cc -> new ChildCommentDto(moimCreatorId, cc)).collect(Collectors.toList());
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

            public ChildCommentDto(Long moimCreatorId, PostComment childComment) {
                this.childCommentId = childComment.getId();
                this.content = childComment.getContent();
                this.depth = childComment.getDepth();
                this.hasDeleted = childComment.isHasDeleted();
                this.reported = childComment.isReported();
                this.createdAt = childComment.getCreatedAt() + "";
                this.updatedAt = childComment.getUpdatedAt() + "";
                this.parentId = childComment.getParent().getId();
                this.commentCreator = new MemberInfoDto(moimCreatorId, childComment.getMember());
            }
        }

    }

}

