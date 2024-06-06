package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.MoimPostWithFileDto;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

@Slf4j
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
        private String moimPostCategory;
        private boolean hasPrivateVisibility;
        private boolean hasFiles;
        private int commentCnt;
        private String createdAt;
        @JsonProperty("memberInfo")
        private PostMemberDto postMemberDto;
        private List<FileDto> files;


        public MoimPostCreateRespDto(MoimPostWithFileDto postWithFiles, Member postCreator) {
            MoimPost moimPost = postWithFiles.getMoimPost();
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory().getValue();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            if (moimPost.getMoim() == null) {
                log.warn("{}, MoimPostCreateRespDto Creator :: {}", this.getClass().getName(), "Moim 이 연관관계되어 있지 않음");
                throw new MoimingApiException(COMMON_INVALID_SITUATION);
            }
            this.postMemberDto = new PostMemberDto(moimPost.getMoim().getCurManagerId(), postCreator);
            if (postWithFiles.getRelatedFiles() != null) {
                this.files = postWithFiles.getRelatedFiles().stream().map(rf -> new FileDto(rf.getFile().getId(), rf.getFileUrl())).collect(Collectors.toList());
            }
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;
            private String memberPfImgUrl;
            private boolean manager;

            public PostMemberDto(Long curManagerId, Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.manager = Objects.equals(curManagerId, member.getId());
                this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                if(StringUtils.hasText(member.getPfImgUrl())) {
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
    }


    @ApiModel(value = "Moim Post API - 응답 - 모임 모든 게시물 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimPostViewRespDto {

        private Long moimPostId;
        private String postTitle;
        private String postContent;
        private String moimPostCategory;
        private int commentCnt;
        private boolean hasPrivateVisibility;
        private boolean hasFiles;
        private String createdAt;
        private String updatedAt;
        @JsonProperty("memberInfo")
        private PostMemberDto postMemberDto;
        private FileDto file;


        public MoimPostViewRespDto(MoimPost moimPost, boolean manager, RelatedFile thumbnailFile) {
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory().getValue();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.updatedAt = moimPost.getUpdatedAt() + "";
            this.postMemberDto = new PostMemberDto(moimPost.getMember(), manager);
            if (thumbnailFile != null) {
                file = new FileDto(thumbnailFile.getFile().getId(), thumbnailFile.getFileUrl());
            }
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;
            private String memberPfImgUrl;
            private boolean manager;

            public PostMemberDto(Member member, boolean manager) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.manager = manager;
                this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                if(StringUtils.hasText(member.getPfImgUrl())) {
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
        private Long updaterId;

        @JsonProperty("memberInfo")
        private PostMemberDto postMemberDto;
        private List<FileDto> files;

        public MoimPostUpdateRespDto(MoimPostWithFileDto postWithFiles) {
            MoimPost moimPost = postWithFiles.getMoimPost();
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory().getValue();
            this.commentCnt = moimPost.getCommentCnt();
            this.hasPrivateVisibility = moimPost.isHasPrivateVisibility();
            this.hasFiles = moimPost.isHasFiles();
            this.createdAt = moimPost.getCreatedAt() + "";
            this.updatedAt = moimPost.getUpdatedAt() + "";
            this.updaterId = moimPost.getUpdaterId();
            if (moimPost.getMoim() == null) {
                log.warn("{}, MoimPostCreateRespDto Creator :: {}", this.getClass().getName(), "Moim 이 연관관계되어 있지 않음");
                throw new MoimingApiException(COMMON_INVALID_SITUATION);
            }
            this.postMemberDto = new PostMemberDto(moimPost.getMoim().getCurManagerId(), moimPost.getMember());
            if (postWithFiles.getRelatedFiles() != null) {
                this.files = postWithFiles.getRelatedFiles().stream().map(rf -> new FileDto(rf.getFile().getId(), rf.getFileUrl())).collect(Collectors.toList());
            }
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;
            private String memberPfImgUrl;
            private boolean manager;


            public PostMemberDto(Long curManagerId, Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.manager = Objects.equals(curManagerId, member.getId());
                this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                if(StringUtils.hasText(member.getPfImgUrl())) {
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
    }


    @ApiModel(value = "Moim Post API - 응답 - 게시물 사진 설정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MoimPostChangeImgRespDto {

        private List<FileDto> files;

        public MoimPostChangeImgRespDto(List<File> savedFiles) {
            this.files = savedFiles.stream().map(FileDto::new).collect(Collectors.toList());
        }

        @Getter
        @Setter
        public static class FileDto {
            private Long fileId;
            private String imgUrl;

            public FileDto(File file) {
                this.fileId = file.getId();
                this.imgUrl = file.getFileUrl();
            }
        }
    }
}