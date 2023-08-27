package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimPostDto {

    private Long moimPostId;
    private String postTitle;
    private String postContent;
    private MoimPostCategory moimPostCategory;
    private boolean isNotice;
    private boolean hasFiles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedMemberId;
    private MoimMemberInfoDto postCreatorInfo;

    /*
     쿼리 외 추가 정보
     */
    private boolean isCreatorCurMember;
    private List<PostFileDto> postFilesDto = new ArrayList<>();
    private List<PostCommentDto> postCommentsDto = new ArrayList<>();

    /*
     Constructor -1
     각 정보들 매핑해서 Dto 형성
     */
    public MoimPostDto(Long moimPostId, String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean isNotice,
                       LocalDateTime createdAt, LocalDateTime updatedAt, Long updatedMemberId, boolean hasFiles, boolean isCreatorCurMember, MoimMemberInfoDto postCreatorInfo) {

        this.moimPostId = moimPostId;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.moimPostCategory = moimPostCategory;
        this.isNotice = isNotice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedMemberId = updatedMemberId;
        this.hasFiles = hasFiles;
        this.isCreatorCurMember = isCreatorCurMember;
        this.postCreatorInfo = postCreatorInfo;

    }

    public static MoimPostDto createMoimPostDto(MoimPost moimPost,boolean isCreatorCurMember) {
        return new MoimPostDto(moimPost.getId()
                , moimPost.getPostTitle()
                , moimPost.getPostContent()
                , moimPost.getMoimPostCategory()
                , moimPost.isNotice()
                , moimPost.getCreatedAt()
                , moimPost.getUpdatedAt()
                , moimPost.getUpdatedMemberId()
                , moimPost.isHasFiles()
                , isCreatorCurMember
                , null
        );
    }


    /*
     Constructor -2
     MoimPost Entity 전달을 통한 Dto 형성
     */
    public MoimPostDto(MoimPost moimPost) {

        this.moimPostId = moimPost.getId();
        this.postTitle = moimPost.getPostTitle();
        this.postContent = moimPost.getPostContent();
        this.moimPostCategory = moimPost.getMoimPostCategory();
        this.isNotice = moimPost.isNotice();
        this.hasFiles = moimPost.isHasFiles();
        this.createdAt = moimPost.getCreatedAt();
        this.updatedAt = moimPost.getUpdatedAt();
        this.updatedMemberId = moimPost.getUpdatedMemberId();
    }

    public void setPostFilesDto(List<PostFileDto> postFilesDto) {
        this.postFilesDto = postFilesDto;
    }

    public void setPostCommentsDto(List<PostCommentDto> postCommentsDto) {
        this.postCommentsDto = postCommentsDto;
    }

    public void setPostCreatorInfo(MoimMemberInfoDto postCreatorInfo) {
        this.postCreatorInfo = postCreatorInfo;
    }
}
