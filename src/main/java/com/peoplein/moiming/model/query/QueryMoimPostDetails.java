package com.peoplein.moiming.model.query;


import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 모임의 게시물들을 전달 (기본 정보)할 수 있도록 정보들을 불러오기 위한 Query DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QueryMoimPostDetails {
    private Long moimPostId;
    private String postTitle;
    private String postContent;
    private MoimPostCategory moimPostCategory;
    private boolean isNotice;
    private boolean hasFiles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedUid;

    // Member
    private Long postCreatorId;

    // MemberInfo
    private MoimMemberInfoDto postCreatorInfoDto;

    public QueryMoimPostDetails(Long moimPostId, String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean isNotice, boolean hasFiles
            , LocalDateTime createdAt, LocalDateTime updatedAt, String updatedUid, Long postCreatorId) {
        this.moimPostId = moimPostId;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.moimPostCategory = moimPostCategory;
        this.isNotice = isNotice;
        this.hasFiles = hasFiles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedUid = updatedUid;
        this.postCreatorId = postCreatorId;
    }

    public void setPostCreatorInfoDto(MoimMemberInfoDto postCreatorInfoDto) {
        this.postCreatorInfoDto = postCreatorInfoDto;
    }
}
