package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.config.AppParams;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.exception.MoimingApiException;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Objects;

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

        public MoimPostCreateRespDto(MoimPost moimPost, Member postCreator) {
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
            this.postMemberDto = new PostMemberDto(moimPost.getMoim().getCreatorId(), postCreator);
        }

        @Getter
        @Setter
        public static class PostMemberDto {
            private Long memberId;
            private String nickname;
            private String memberPfImgUrl;
            private boolean creator;

            public PostMemberDto(Long moimCreatorId, Member member) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.creator = Objects.equals(moimCreatorId, member.getId());
                this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                if (member.getMemberInfo() != null) { // NULL 인 경우는 발생할 수가 없다
                    if (StringUtils.hasText(member.getMemberInfo().getPfImgUrl())) {
                        this.memberPfImgUrl = member.getMemberInfo().getPfImgUrl();
                    }
                }
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


        public MoimPostViewRespDto(MoimPost moimPost, boolean creator) {
            this.moimPostId = moimPost.getId();
            this.postTitle = moimPost.getPostTitle();
            this.postContent = moimPost.getPostContent();
            this.moimPostCategory = moimPost.getMoimPostCategory().getValue();
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
            private String memberPfImgUrl;
            private boolean creator;

            public PostMemberDto(Member member, boolean creator) {
                this.memberId = member.getId();
                this.nickname = member.getNickname();
                this.creator = creator;
                this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                if (member.getMemberInfo() != null) { // NULL 인 경우는  DORMANT 혹은 DELETED
                    if (StringUtils.hasText(member.getMemberInfo().getPfImgUrl())) {
                        this.memberPfImgUrl = member.getMemberInfo().getPfImgUrl();
                    }
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
                if (moimPost.getMoim() == null) {
                    log.warn("{}, MoimPostCreateRespDto Creator :: {}", this.getClass().getName(), "Moim 이 연관관계되어 있지 않음");
                    throw new MoimingApiException(COMMON_INVALID_SITUATION);
                }
                this.postMemberDto = new PostMemberDto(moimPost.getMoim().getCreatorId(), moimPost.getMember());
            }

            @Getter
            @Setter
            public static class PostMemberDto {
                private Long memberId;
                private String nickname;
                private String memberPfImgUrl;
                private boolean creator;


                public PostMemberDto(Long moimCreatorId, Member member) {
                    this.memberId = member.getId();
                    this.nickname = member.getNickname();
                    this.memberPfImgUrl = AppParams.DEFAULT_MEMBER_PF_IMG_PATH;
                    this.creator = Objects.equals(moimCreatorId, member.getId());
                    if (member.getMemberInfo() != null) { // NULL 인 경우 DELETE 혹은 DORMANT
                        if (StringUtils.hasText(member.getMemberInfo().getPfImgUrl())) {
                            this.memberPfImgUrl = member.getMemberInfo().getPfImgUrl();
                        }
                    }
                }
            }
        }
    }
}
