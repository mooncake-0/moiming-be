package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.ContentStatus;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.MoimPostWithFileDto;
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
    private Long updaterId;
    private MemberInfoDto postCreator;
    private List<ParentCommentDto> postComments;
    private List<FileDto> files;
    public MoimPostDetailViewRespDto(MoimPostWithFileDto postWithFiles, List<PostComment> parentComments, Map<Long, List<PostComment>> inputMap) {

        MoimPost moimPost = postWithFiles.getMoimPost();
        this.moimPostId = moimPost.getId();
        this.postTitle = moimPost.getPostTitle();
        this.postContent = moimPost.getPostContent();
        this.postCategory = moimPost.getMoimPostCategory().getValue();
        this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
        this.hasFiles = moimPost.isHasFiles();
        this.commentCnt = moimPost.getCommentCnt();
        this.createdAt = moimPost.getCreatedAt() + "";
        this.updatedAt = moimPost.getUpdatedAt() + "";
        this.updaterId = moimPost.getUpdaterId();

        Moim moim = moimPost.getMoim();
        if (moim == null) {
            log.warn("{}, MoimPostCreateRespDto Creator :: {}", this.getClass().getName(), "Moim 이 연관관계되어 있지 않음");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }
        this.postCreator = new MemberInfoDto(moim.getCurManagerId(), moimPost.getMember());
        this.postComments = parentComments.stream().map(parent -> new ParentCommentDto(moim.getCurManagerId(), parent, inputMap.get(parent.getId()))).collect(Collectors.toList());
        if (postWithFiles.getRelatedFiles() != null) {
            this.files = postWithFiles.getRelatedFiles().stream().map(rf -> new FileDto(rf.getFile().getId(), rf.getFileUrl())).collect(Collectors.toList());
        }
    }


    @Getter
    @Setter
    public static class MemberInfoDto {

        private Long memberId;
        private String nickname;
        private String memberPfImgUrl;
        private boolean manager;

        public MemberInfoDto(Long curManagerId, Member member) {
            this.memberId = member.getId();
            this.nickname = member.getNickname();
            this.manager = Objects.equals(curManagerId, member.getId());
            this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
            if (StringUtils.hasText(member.getPfImgUrl())) {
                this.memberPfImgUrl = member.getPfImgUrl();
            }
        }
    }

    @Getter
    @Setter
    public static class FileDto {
        private Long fileId;
        private String imgUrl;

        public FileDto(Long fileId, String imgUrl) {
            this.fileId = fileId;
            this.imgUrl = imgUrl;
        }
    }


    @Getter
    @Setter
    public static class ParentCommentDto {

        private Long parentCommentId;
        private String content;
        private int depth;
        private boolean deleted;
        private boolean reportHidden;
        private String createdAt;
        private String updatedAt;
        private MemberInfoDto commentCreator;
        private List<ChildCommentDto> childComments;

        public ParentCommentDto(Long curManagerId, PostComment parentComment, List<PostComment> childComments) {
            this.parentCommentId = parentComment.getId();
            this.content = parentComment.getContent();
            this.depth = parentComment.getDepth();
            this.deleted = parentComment.isHasDeleted();
            this.reportHidden = parentComment.getContentStatus() == ContentStatus.HIDDEN;
            this.createdAt = parentComment.getCreatedAt() + "";
            this.updatedAt = parentComment.getUpdatedAt() + "";
            this.commentCreator = new MemberInfoDto(curManagerId, parentComment.getMember());
            this.childComments = childComments.stream().map(cc -> new ChildCommentDto(curManagerId, cc)).collect(Collectors.toList());
        }


        @Getter
        @Setter
        public static class ChildCommentDto {

            private Long childCommentId;
            private String content;
            private int depth;
            private boolean deleted;
            private boolean reportHidden;
            private String createdAt;
            private String updatedAt;
            private Long parentId;
            private MemberInfoDto commentCreator;

            public ChildCommentDto(Long curManagerId, PostComment childComment) {
                this.childCommentId = childComment.getId();
                this.content = childComment.getContent();
                this.depth = childComment.getDepth();
                this.deleted = childComment.isHasDeleted();
                this.reportHidden = childComment.getContentStatus() == ContentStatus.HIDDEN;
                this.createdAt = childComment.getCreatedAt() + "";
                this.updatedAt = childComment.getUpdatedAt() + "";
                this.parentId = childComment.getParent().getId();
                this.commentCreator = new MemberInfoDto(curManagerId, childComment.getMember());
            }
        }

    }

}

